# TicketFlow — Java 21 / Spring Boot Implementation Guide

Companion to `ticketflow-spec.md`. Stack: Java 21, Spring Boot 3.x,
`JdbcTemplate` (deliberately not Spring Data JPA — event stores are
append-only logs, not entity graphs, and JPA's change-tracking model fights
that), Postgres. Uses Java 21 records, sealed interfaces, and pattern
matching for switch to keep the event model concise.

---

## 0. Project Setup

`pom.xml` essentials:

```xml
<properties>
  <java.version>21</java.version>
</properties>
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
  </dependency>
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
  </dependency>
  <dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

`application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ticketflow
    username: ticketflow
    password: ticketflow
  flyway:
    locations: classpath:db/migration
```

---

## 1. Event Model

Java 21's sealed interfaces + records give you a closed, exhaustive event
hierarchy — the compiler forces you to handle every event type in `apply`,
which is exactly the safety net you want.

```java
// domain/event/DomainEvent.java
package com.ticketflow.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public sealed interface DomainEvent permits
    TicketOpened, TicketAssigned, CommentAdded, PriorityChanged,
    TicketResolved, TicketReopened, TicketClosed, TicketAutoClosed,
    AgentRegistered, AgentWorkloadIncreased, AgentWorkloadDecreased {

  UUID eventId();
  UUID aggregateId();
  int sequenceNumber();
  int eventVersion();
  Instant createdAt();
  Map<String, Object> metadata();
}
```

```java
// domain/event/TicketEvents.java
package com.ticketflow.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TicketOpened(
    UUID eventId, UUID aggregateId, int sequenceNumber, int eventVersion,
    Instant createdAt, Map<String, Object> metadata,
    String title, String description, UUID reporterId, String priority
) implements DomainEvent {}

public record TicketAssigned(
    UUID eventId, UUID aggregateId, int sequenceNumber, int eventVersion,
    Instant createdAt, Map<String, Object> metadata, UUID agentId
) implements DomainEvent {}

public record CommentAdded(
    UUID eventId, UUID aggregateId, int sequenceNumber, int eventVersion,
    Instant createdAt, Map<String, Object> metadata,
    UUID authorId, String text, boolean isInternal
) implements DomainEvent {}

public record PriorityChanged(
    UUID eventId, UUID aggregateId, int sequenceNumber, int eventVersion,
    Instant createdAt, Map<String, Object> metadata,
    String newPriority, UUID changedBy
) implements DomainEvent {}

public record TicketResolved(
    UUID eventId, UUID aggregateId, int sequenceNumber, int eventVersion,
    Instant createdAt, Map<String, Object> metadata, String resolutionNote
) implements DomainEvent {}

public record TicketReopened(
    UUID eventId, UUID aggregateId, int sequenceNumber, int eventVersion,
    Instant createdAt, Map<String, Object> metadata, String reason
) implements DomainEvent {}

public record TicketClosed(
    UUID eventId, UUID aggregateId, int sequenceNumber, int eventVersion,
    Instant createdAt, Map<String, Object> metadata, UUID closedBy // nullable
) implements DomainEvent {}

public record TicketAutoClosed(
    UUID eventId, UUID aggregateId, int sequenceNumber, int eventVersion,
    Instant createdAt, Map<String, Object> metadata
) implements DomainEvent {}
```

*(`AgentRegistered`, `AgentWorkloadIncreased`, `AgentWorkloadDecreased` follow
the same shape — omitted for brevity, same pattern.)*

Note each record carries its own `eventType` implicitly via its class name —
you don't need a separate `eventType` string field in Java the way you might
in a dynamically-typed language, since `event.getClass().getSimpleName()` (or
a small explicit mapping, safer across refactors) gives you that on demand.

---

## 2. Aggregate Root

```java
// domain/AggregateRoot.java
package com.ticketflow.domain;

import com.ticketflow.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AggregateRoot<E extends DomainEvent> {
  protected UUID id;
  protected int version = -1; // last applied sequence number
  private final List<E> uncommitted = new ArrayList<>();

  protected void raise(E event) {
    apply(event);
    uncommitted.add(event);
  }

  // Must be pure: no validation, no exceptions, no side effects — only ever
  // mutate fields based on the event's payload. Runs identically for a brand
  // new event and for the 400th event during a historical replay.
  protected abstract void apply(E event);

  public void loadFromHistory(List<E> history) {
    for (E event : history) apply(event);
  }

  public List<E> getUncommittedEvents() { return List.copyOf(uncommitted); }
  public void clearUncommittedEvents() { uncommitted.clear(); }
  public UUID getId() { return id; }
  public int getVersion() { return version; }
}
```

---

## 3. `Ticket` Aggregate

Java 21's pattern matching for `switch` over the sealed `DomainEvent`
interface gives you compiler-enforced exhaustiveness in `apply` — leave a
case out and the build fails, which is exactly the guarantee you want for
something this easy to get subtly wrong.

```java
// domain/Ticket.java
package com.ticketflow.domain;

import com.ticketflow.domain.event.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Ticket extends AggregateRoot<DomainEvent> {

  public enum Status { OPEN, ASSIGNED, RESOLVED, CLOSED }
  public enum Priority { LOW, MEDIUM, HIGH, URGENT }

  private Status status = Status.OPEN;
  private UUID assignedAgentId;
  private UUID reporterId;
  private Priority priority;
  private int reopenCount = 0;

  public static Ticket open(UUID id, String title, String description, UUID reporterId, Priority priority) {
    if (title == null || title.isBlank()) throw new IllegalArgumentException("Title required");
    Ticket t = new Ticket();
    t.id = id;
    t.raise(new TicketOpened(
        UUID.randomUUID(), id, t.version + 1, 1, Instant.now(), Map.of(),
        title, description, reporterId, priority.name()
    ));
    return t;
  }

  public void assign(UUID agentId) {
    assertNotClosed();
    if (agentId.equals(assignedAgentId)) throw new IllegalStateException("Already assigned to this agent");
    raise(new TicketAssigned(UUID.randomUUID(), id, version + 1, 1, Instant.now(), Map.of(), agentId));
  }

  public void addComment(UUID authorId, String text, boolean isInternal) {
    assertNotClosed();
    raise(new CommentAdded(UUID.randomUUID(), id, version + 1, 1, Instant.now(), Map.of(), authorId, text, isInternal));
  }

  public void changePriority(Priority newPriority, UUID changedBy, boolean changerIsAgent) {
    if (newPriority == Priority.URGENT && !changerIsAgent) {
      throw new IllegalStateException("Only agents may escalate to urgent");
    }
    raise(new PriorityChanged(UUID.randomUUID(), id, version + 1, 1, Instant.now(), Map.of(), newPriority.name(), changedBy));
  }

  public void resolve(String resolutionNote) {
    if (status != Status.ASSIGNED) throw new IllegalStateException("Ticket must be assigned before resolving");
    raise(new TicketResolved(UUID.randomUUID(), id, version + 1, 1, Instant.now(), Map.of(), resolutionNote));
  }

  public void reopen(String reason) {
    if (status != Status.RESOLVED) throw new IllegalStateException("Only resolved tickets can reopen");
    if (reopenCount >= 3) throw new IllegalStateException("Max reopen limit reached");
    raise(new TicketReopened(UUID.randomUUID(), id, version + 1, 1, Instant.now(), Map.of(), reason));
  }

  public void close(UUID closedBy) {
    if (status != Status.RESOLVED) throw new IllegalStateException("Ticket must be resolved before closing");
    raise(new TicketClosed(UUID.randomUUID(), id, version + 1, 1, Instant.now(), Map.of(), closedBy));
  }

  private void assertNotClosed() {
    if (status == Status.CLOSED) throw new IllegalStateException("Ticket is closed");
  }

  @Override
  protected void apply(DomainEvent event) {
    switch (event) {
      case TicketOpened e -> {
        reporterId = e.reporterId();
        priority = Priority.valueOf(e.priority());
        status = Status.OPEN;
      }
      case TicketAssigned e -> {
        assignedAgentId = e.agentId();
        status = Status.ASSIGNED;
      }
      case CommentAdded e -> { /* no state change beyond history */ }
      case PriorityChanged e -> priority = Priority.valueOf(e.newPriority());
      case TicketResolved e -> status = Status.RESOLVED;
      case TicketReopened e -> { status = Status.ASSIGNED; reopenCount++; }
      case TicketClosed e -> status = Status.CLOSED;
      case TicketAutoClosed e -> status = Status.CLOSED;
      default -> throw new IllegalStateException("Unhandled event for Ticket: " + event);
    }
    version = event.sequenceNumber();
  }

  // getters for projections / API responses
  public Status getStatus() { return status; }
  public UUID getAssignedAgentId() { return assignedAgentId; }
  public Priority getPriority() { return priority; }
}
```

The `default ->` branch is a safety net for events belonging to *other*
aggregates accidentally reaching this `apply` — it should never fire if your
repository only ever loads a single aggregate's stream, but it turns a silent
bug into a loud one if that assumption is ever violated.

---

## 4. Event Store

```sql
-- src/main/resources/db/migration/V1__create_events.sql
CREATE TABLE events (
  event_id UUID PRIMARY KEY,
  aggregate_id UUID NOT NULL,
  aggregate_type TEXT NOT NULL,
  event_type TEXT NOT NULL,
  event_version INT NOT NULL,
  sequence_number INT NOT NULL,
  payload JSONB NOT NULL,
  metadata JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  global_seq BIGSERIAL,
  UNIQUE (aggregate_id, sequence_number)
);
CREATE INDEX idx_events_aggregate ON events (aggregate_id, sequence_number);
CREATE INDEX idx_events_global_seq ON events (global_seq);
```

The `global_seq BIGSERIAL` is the "do it properly from day one" fix
mentioned in the TypeScript guide — projections page through it instead of
using `OFFSET`, which stays correct and fast as the table grows.

```java
// infra/EventStore.java
package com.ticketflow.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketflow.domain.event.DomainEvent;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Component
public class EventStore {
  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;
  private final EventTypeRegistry registry; // maps event class <-> "TicketOpened" string, and back for reads

  public EventStore(JdbcTemplate jdbc, ObjectMapper mapper, EventTypeRegistry registry) {
    this.jdbc = jdbc; this.mapper = mapper; this.registry = registry;
  }

  @Transactional
  public void append(UUID aggregateId, String aggregateType, int expectedVersion, List<DomainEvent> events) {
    for (DomainEvent event : events) {
      try {
        jdbc.update("""
            INSERT INTO events
            (event_id, aggregate_id, aggregate_type, event_type, event_version,
             sequence_number, payload, metadata, created_at)
            VALUES (?,?,?,?,?,?,?::jsonb,?::jsonb,?)
            """,
            event.eventId(), aggregateId, aggregateType, registry.typeNameOf(event),
            event.eventVersion(), event.sequenceNumber(),
            toJson(event), toJson(event.metadata()), Timestamp.from(event.createdAt())
        );
      } catch (DuplicateKeyException ex) {
        throw new ConcurrencyException(
            "Concurrent write conflict on aggregate %s at sequence %d"
                .formatted(aggregateId, event.sequenceNumber()), ex);
      }
    }
    // Outbox insert can go here, in the SAME transaction — see section 10
  }

  public List<DomainEvent> readStream(UUID aggregateId, int fromVersion) {
    return jdbc.query("""
        SELECT * FROM events WHERE aggregate_id = ? AND sequence_number >= ?
        ORDER BY sequence_number
        """,
        (rs, rowNum) -> registry.deserialize(rs, mapper),
        aggregateId, fromVersion
    );
  }

  public List<DomainEvent> readAllSince(long globalSeq, int limit) {
    return jdbc.query("""
        SELECT * FROM events WHERE global_seq > ? ORDER BY global_seq LIMIT ?
        """,
        (rs, rowNum) -> registry.deserialize(rs, mapper),
        globalSeq, limit
    );
  }

  private String toJson(Object o) {
    try { return mapper.writeValueAsString(o); }
    catch (Exception e) { throw new RuntimeException(e); }
  }
}
```

```java
// infra/ConcurrencyException.java
package com.ticketflow.infra;

public class ConcurrencyException extends RuntimeException {
  public ConcurrencyException(String message, Throwable cause) { super(message, cause); }
}
```

`DuplicateKeyException` is Spring's translation of Postgres's `23505`
unique-violation error via `UNIQUE (aggregate_id, sequence_number)` — the
same guarantee as the TypeScript version, just surfaced through Spring's
exception-translation layer instead of a raw error code check.

### `EventTypeRegistry` — the Java-specific piece

Since events are concrete record classes rather than a generic `{eventType,
payload}` shape, you need one small component to map a stored `event_type`
string back to the right record class for deserialization:

```java
// infra/EventTypeRegistry.java
package com.ticketflow.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketflow.domain.event.*;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class EventTypeRegistry {
  private final Map<String, Class<? extends DomainEvent>> byName = Map.ofEntries(
      Map.entry("TicketOpened", TicketOpened.class),
      Map.entry("TicketAssigned", TicketAssigned.class),
      Map.entry("CommentAdded", CommentAdded.class),
      Map.entry("PriorityChanged", PriorityChanged.class),
      Map.entry("TicketResolved", TicketResolved.class),
      Map.entry("TicketReopened", TicketReopened.class),
      Map.entry("TicketClosed", TicketClosed.class),
      Map.entry("TicketAutoClosed", TicketAutoClosed.class)
      // ... Agent events
  );

  public String typeNameOf(DomainEvent event) { return event.getClass().getSimpleName(); }

  public DomainEvent deserialize(ResultSet rs, ObjectMapper mapper) throws SQLException {
    String typeName = rs.getString("event_type");
    Class<? extends DomainEvent> clazz = byName.get(typeName);
    if (clazz == null) throw new IllegalStateException("Unknown event type: " + typeName);
    try {
      // Merge top-level columns with the JSON payload into one node, since the
      // record's fields (eventId, aggregateId, etc.) live in dedicated columns
      // while domain-specific fields live in the `payload` JSONB column.
      var node = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(rs.getString("payload"));
      node.put("eventId", rs.getString("event_id"));
      node.put("aggregateId", rs.getString("aggregate_id"));
      node.put("sequenceNumber", rs.getInt("sequence_number"));
      node.put("eventVersion", rs.getInt("event_version"));
      node.put("createdAt", rs.getTimestamp("created_at").toInstant().toString());
      // metadata merged similarly, or nested under a "metadata" key depending on your Jackson config
      return mapper.treeToValue(node, clazz);
    } catch (Exception e) {
      throw new RuntimeException("Failed to deserialize event " + typeName, e);
    }
  }
}
```

This registry is also exactly where **upcasting** slots in later (section 9)
— `deserialize` is the single chokepoint every stored event passes through
on the way back into the domain model.

---

## 5. Repository + Retry-on-Conflict

```java
// application/TicketRepository.java
package com.ticketflow.application;

import com.ticketflow.domain.Ticket;
import com.ticketflow.domain.event.DomainEvent;
import com.ticketflow.infra.EventStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class TicketRepository {
  private final EventStore store;
  private final ApplicationEventPublisher publisher; // drives projections + process managers

  public TicketRepository(EventStore store, ApplicationEventPublisher publisher) {
    this.store = store; this.publisher = publisher;
  }

  public Ticket load(UUID id) {
    List<DomainEvent> events = store.readStream(id, 0);
    if (events.isEmpty()) throw new TicketNotFoundException(id);
    Ticket ticket = new Ticket();
    ticket.loadFromHistory(events);
    return ticket;
  }

  public void save(Ticket ticket) {
    List<DomainEvent> events = ticket.getUncommittedEvents();
    if (events.isEmpty()) return;
    int expectedVersion = ticket.getVersion() - events.size();
    store.append(ticket.getId(), "Ticket", expectedVersion, List.copyOf(events));
    ticket.clearUncommittedEvents();
    events.forEach(publisher::publishEvent); // Spring's own event bus for in-process fan-out
  }
}
```

Using `ApplicationEventPublisher` here is the Spring-native way to fan events
out to projections and process managers in-process — for a project this
size, you don't need Kafka; `@EventListener`/`@TransactionalEventListener`
methods are enough (see section 6).

```java
// application/CommandExecutor.java
package com.ticketflow.application;

import com.ticketflow.infra.ConcurrencyException;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class CommandExecutor {
  public <T> void execute(Supplier<T> load, Consumer<T> mutate, Consumer<T> save) {
    execute(load, mutate, save, 3);
  }

  public <T> void execute(Supplier<T> load, Consumer<T> mutate, Consumer<T> save, int maxRetries) {
    for (int attempt = 0; attempt < maxRetries; attempt++) {
      T aggregate = load.get();
      mutate.accept(aggregate);
      try {
        save.accept(aggregate);
        return;
      } catch (ConcurrencyException ex) {
        if (attempt == maxRetries - 1) throw ex;
        // loop again: reload and reapply
      }
    }
  }
}
```

Usage in a controller/service:

```java
@Service
public class TicketCommandService {
  private final TicketRepository repo;
  private final CommandExecutor executor;

  public TicketCommandService(TicketRepository repo, CommandExecutor executor) {
    this.repo = repo; this.executor = executor;
  }

  public void assignTicket(UUID ticketId, UUID agentId) {
    executor.execute(
        () -> repo.load(ticketId),
        ticket -> ticket.assign(agentId),
        repo::save
    );
  }
}
```

**Day 2 concurrency test (JUnit 5 + `CompletableFuture` for concurrent load):**

```java
@Test
void concurrentCommentsResolveViaRetryWithNoLostUpdates() throws Exception {
  UUID ticketId = openTestTicket();
  int concurrency = 20;
  var futures = IntStream.range(0, concurrency)
      .mapToObj(i -> CompletableFuture.runAsync(() ->
          executor.execute(
              () -> repo.load(ticketId),
              ticket -> ticket.addComment(UUID.randomUUID(), "comment " + i, false),
              repo::save
          )
      )).toList();

  futures.forEach(CompletableFuture::join); // all should complete without throwing

  var events = eventStore.readStream(ticketId, 0);
  long commentCount = events.stream().filter(e -> e instanceof CommentAdded).count();
  assertThat(commentCount).isEqualTo(concurrency); // none lost to the race
}
```

---

## 6. Projections via Spring's `@TransactionalEventListener`

```java
// read/projection/OpenTicketsByAgentProjection.java
package com.ticketflow.read.projection;

import com.ticketflow.domain.event.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OpenTicketsByAgentProjection {
  private static final String NAME = "open_tickets_by_agent";
  private final JdbcTemplate jdbc;

  public OpenTicketsByAgentProjection(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(DomainEvent event) {
    if (alreadyProcessed(event)) return;
    switch (event) {
      case TicketOpened e -> jdbc.update("""
          INSERT INTO open_tickets_by_agent (ticket_id, title, priority, status)
          VALUES (?,?,?,'OPEN') ON CONFLICT (ticket_id) DO NOTHING
          """, e.aggregateId(), e.title(), e.priority());
      case TicketAssigned e -> jdbc.update("""
          UPDATE open_tickets_by_agent SET agent_id = ?, status = 'ASSIGNED' WHERE ticket_id = ?
          """, e.agentId(), e.aggregateId());
      case TicketResolved e -> jdbc.update(
          "DELETE FROM open_tickets_by_agent WHERE ticket_id = ?", e.aggregateId());
      case TicketClosed e -> jdbc.update(
          "DELETE FROM open_tickets_by_agent WHERE ticket_id = ?", e.aggregateId());
      case TicketAutoClosed e -> jdbc.update(
          "DELETE FROM open_tickets_by_agent WHERE ticket_id = ?", e.aggregateId());
      default -> { /* not relevant to this projection */ }
    }
    markProcessed(event);
  }

  private boolean alreadyProcessed(DomainEvent event) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM processed_events WHERE projection_name = ? AND event_id = ?",
        Integer.class, NAME, event.eventId());
    return count != null && count > 0;
  }

  private void markProcessed(DomainEvent event) {
    jdbc.update("INSERT INTO processed_events (projection_name, event_id) VALUES (?,?)",
        NAME, event.eventId());
  }
}
```

**Why `AFTER_COMMIT`:** the repository publishes events via
`ApplicationEventPublisher` inside the same call as the DB write. Using
`@TransactionalEventListener(phase = AFTER_COMMIT)` instead of a plain
`@EventListener` guarantees the projection only reacts once the event row is
durably committed — if the transaction rolls back, the projection never runs
at all, which avoids a whole class of "read model saw something the write
side later undid" bugs.

### Rebuild endpoint/CLI runner

```java
// read/RebuildProjectionsRunner.java
package com.ticketflow.read;

import com.ticketflow.infra.EventStore;
import com.ticketflow.domain.event.DomainEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RebuildProjectionsRunner {
  private final EventStore store;
  private final JdbcTemplate jdbc;
  private final List<ProjectionHandler> projections; // one interface, all projections implement it

  public RebuildProjectionsRunner(EventStore store, JdbcTemplate jdbc, List<ProjectionHandler> projections) {
    this.store = store; this.jdbc = jdbc; this.projections = projections;
  }

  public void rebuildAll() {
    for (ProjectionHandler p : projections) {
      jdbc.update("TRUNCATE " + p.tableName());
      jdbc.update("DELETE FROM processed_events WHERE projection_name = ?", p.name());
    }
    long globalSeq = 0;
    List<DomainEvent> batch;
    int total = 0;
    do {
      batch = store.readAllSince(globalSeq, 500);
      for (DomainEvent event : batch) {
        for (ProjectionHandler p : projections) p.handle(event);
      }
      total += batch.size();
      // advance globalSeq using the last event's stored global_seq (fetch alongside the event, or track separately)
    } while (!batch.isEmpty());
    System.out.printf("Rebuilt %d projections from %d events%n", projections.size(), total);
  }
}
```

Wire this behind an admin REST endpoint or a `CommandLineRunner` with a
`--rebuild-projections` flag — either is fine for a practice project.

---

## 7. `Agent` Aggregate + Process Manager

Same shape as `Ticket` — a sealed `apply` switch, commands that validate then
raise. The interesting part is the process manager:

```java
// application/AssignmentTracker.java
package com.ticketflow.application;

import com.ticketflow.domain.Agent;
import com.ticketflow.domain.Ticket;
import com.ticketflow.domain.event.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class AssignmentTracker {
  private final AgentRepository agentRepo;
  private final TicketRepository ticketRepo;
  private final CommandExecutor executor;
  private final JdbcTemplate jdbc;

  public AssignmentTracker(AgentRepository agentRepo, TicketRepository ticketRepo,
                            CommandExecutor executor, JdbcTemplate jdbc) {
    this.agentRepo = agentRepo; this.ticketRepo = ticketRepo;
    this.executor = executor; this.jdbc = jdbc;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(DomainEvent event) {
    switch (event) {
      case TicketAssigned e -> increaseWorkload(e.agentId(), e.aggregateId());
      case TicketReopened e -> increaseWorkload(currentAgentFor(e.aggregateId()), e.aggregateId());
      case TicketResolved e -> decreaseWorkload(currentAgentFor(e.aggregateId()));
      case TicketClosed e -> decreaseWorkload(currentAgentFor(e.aggregateId()));
      default -> { }
    }
  }

  private void increaseWorkload(UUID agentId, UUID ticketId) {
    try {
      executor.execute(() -> agentRepo.load(agentId), Agent::increaseWorkload, agentRepo::save);
    } catch (Exception ex) {
      // Compensation: go back through Ticket's own command path, never write
      // events directly, so version/sequence invariants stay intact.
      executor.execute(
          () -> ticketRepo.load(ticketId),
          ticket -> ticket.recordAssignmentFailure(ex.getMessage()), // small additional command on Ticket
          ticketRepo::save
      );
    }
  }

  private void decreaseWorkload(UUID agentId) {
    if (agentId == null) return;
    executor.execute(() -> agentRepo.load(agentId), Agent::decreaseWorkload, agentRepo::save);
  }

  private UUID currentAgentFor(UUID ticketId) {
    return jdbc.queryForObject(
        "SELECT agent_id FROM open_tickets_by_agent WHERE ticket_id = ?", UUID.class, ticketId);
  }
}
```

---

## 8. Background Auto-Close Job with `@Scheduled`

```java
// jobs/AutoCloseJob.java
package com.ticketflow.jobs;

import com.ticketflow.application.CommandExecutor;
import com.ticketflow.application.TicketRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AutoCloseJob {
  private final JdbcTemplate jdbc;
  private final TicketRepository repo;
  private final CommandExecutor executor;

  public AutoCloseJob(JdbcTemplate jdbc, TicketRepository repo, CommandExecutor executor) {
    this.jdbc = jdbc; this.repo = repo; this.executor = executor;
  }

  @Scheduled(cron = "0 0 * * * *") // hourly; fine for a practice project
  public void run() {
    List<UUID> candidates = jdbc.queryForList("""
        SELECT ticket_id FROM ticket_timeline t1
        WHERE event_type = 'TicketResolved'
          AND occurred_at < now() - interval '7 days'
          AND NOT EXISTS (
            SELECT 1 FROM ticket_timeline t2
            WHERE t2.ticket_id = t1.ticket_id
              AND t2.event_type IN ('TicketReopened','TicketClosed','TicketAutoClosed')
              AND t2.occurred_at > t1.occurred_at
          )
        """, UUID.class);

    for (UUID ticketId : candidates) {
      try {
        executor.execute(() -> repo.load(ticketId), ticket -> ticket.close(null), repo::save);
      } catch (Exception ex) {
        // log and continue — one bad ticket shouldn't block the batch
      }
    }
  }
}

// Enable scheduling in your main application class:
// @SpringBootApplication @EnableScheduling
// public class TicketFlowApplication { ... }
```

The query itself stays idempotent regardless of how `@Scheduled` overlaps
with retries or restarts — a ticket already closed or reopened after
resolution simply won't be selected again.

---

## 9. Point-in-Time Reconstruction

```java
// api/TicketHistoryController.java
package com.ticketflow.api;

import com.ticketflow.domain.Ticket;
import com.ticketflow.domain.event.DomainEvent;
import com.ticketflow.infra.EventStore;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketHistoryController {
  private final EventStore store;

  public TicketHistoryController(EventStore store) { this.store = store; }

  @GetMapping("/{id}/at/{timestamp}")
  public TicketView getTicketAt(@PathVariable UUID id, @PathVariable String timestamp) {
    Instant at = Instant.parse(timestamp);
    List<DomainEvent> events = store.readStream(id, 0).stream()
        .filter(e -> !e.createdAt().isAfter(at))
        .toList();
    if (events.isEmpty()) throw new TicketNotFoundAtTimeException(id, at);
    Ticket ticket = new Ticket();
    ticket.loadFromHistory(events);
    return TicketView.from(ticket); // small DTO exposing status/priority/assignedAgentId etc.
  }
}
```

Same `loadFromHistory` used for every other read path — filtering the event
list by timestamp before replay is the entire feature.

---

## 10. Snapshots

```sql
CREATE TABLE snapshots (
  aggregate_id UUID,
  version INT,
  state JSONB,
  created_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (aggregate_id, version)
);
```

```java
public Ticket load(UUID id) {
  var snapshot = snapshotStore.latest(id); // Optional<SnapshotRow>
  Ticket ticket = new Ticket();
  int fromVersion = 0;
  if (snapshot.isPresent()) {
    ticket.hydrateFromSnapshot(snapshot.get().state(), snapshot.get().version());
    fromVersion = snapshot.get().version() + 1;
  }
  ticket.loadFromHistory(store.readStream(id, fromVersion));
  return ticket;
}

// in save(), after appending:
if (ticket.getVersion() % 50 == 0) {
  snapshotStore.save(ticket.getId(), ticket.getVersion(), ticket.toSnapshotState());
}
```

`hydrateFromSnapshot`/`toSnapshotState` are a small pair of methods on
`Ticket` that (de)serialize its private fields directly — Jackson can handle
this via a package-private constructor or a dedicated `@JsonCreator`, your
choice.

---

## 11. Upcasting for Schema Evolution

The chokepoint is `EventTypeRegistry.deserialize` from section 4 — insert an
upcast step there:

```java
// infra/Upcasters.java
package com.ticketflow.infra;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.UnaryOperator;

@Component
public class Upcasters {
  // keyed by "EventType" -> "fromVersion" -> transform function
  private final Map<String, Map<Integer, UnaryOperator<ObjectNode>>> chains = Map.of(
      "CommentAdded", Map.of(
          1, node -> node.put("format", "plain") // v1 had no `format` field; default it
      )
  );

  public ObjectNode upcast(String eventType, int fromVersion, ObjectNode node) {
    var chain = chains.get(eventType);
    int version = fromVersion;
    while (chain != null && chain.containsKey(version)) {
      node = chain.get(version).apply(node);
      version++;
    }
    return node;
  }
}
```

Call `upcasters.upcast(typeName, eventVersion, node)` right before
`mapper.treeToValue(node, clazz)` in `EventTypeRegistry.deserialize`. Test by
inserting a raw `event_version = 1` row via `jdbc.update` directly in a test,
then asserting `readStream` returns a fully-formed `CommentAdded` with
`format = "plain"` even though that field never existed in the original
payload.

---

## 12. Outbox Pattern

```sql
CREATE TABLE outbox (
  id BIGSERIAL PRIMARY KEY,
  event_id UUID,
  destination TEXT,
  payload JSONB,
  published_at TIMESTAMPTZ
);
```

Insert into `outbox` inside the same `@Transactional` method as the event
append in `EventStore.append` — same connection, same transaction, so it
commits or rolls back atomically with the event itself.

```java
// jobs/OutboxRelay.java
@Component
public class OutboxRelay {
  private final JdbcTemplate jdbc;
  private final NotificationClient notifications;

  @Scheduled(fixedDelay = 2000)
  public void relay() {
    var rows = jdbc.queryForList(
        "SELECT * FROM outbox WHERE published_at IS NULL ORDER BY id LIMIT 100");
    for (var row : rows) {
      notifications.send(row); // external call — deliberately OUTSIDE any DB transaction
      jdbc.update("UPDATE outbox SET published_at = now() WHERE id = ?", row.get("id"));
    }
  }
}
```

Same at-least-once caveat as the TypeScript version: `NotificationClient`
should dedupe on `event_id` on its own end, since a crash between "sent" and
"marked published" means the relay will resend on the next pass.

---

## 13. Suggested Package Layout

```
src/main/java/com/ticketflow/
  domain/
    AggregateRoot.java
    Ticket.java
    Agent.java
    event/
      DomainEvent.java
      TicketEvents.java
      AgentEvents.java
  infra/
    EventStore.java
    EventTypeRegistry.java
    Upcasters.java
    ConcurrencyException.java
  application/
    TicketRepository.java
    AgentRepository.java
    CommandExecutor.java
    TicketCommandService.java
    AssignmentTracker.java
  read/
    ProjectionHandler.java
    projection/
      OpenTicketsByAgentProjection.java
      TicketTimelineProjection.java
      SlaTrackingProjection.java
      AgentWorkloadProjection.java
    RebuildProjectionsRunner.java
  jobs/
    AutoCloseJob.java
    OutboxRelay.java
  api/
    TicketController.java
    TicketHistoryController.java
src/main/resources/
  db/migration/
    V1__create_events.sql
    V2__create_projections.sql
    V3__create_outbox_and_snapshots.sql
src/test/java/com/ticketflow/
  TicketTest.java
  ConcurrencyTest.java
  ProjectionRebuildTest.java
  UpcastingTest.java
```

Same CQRS split as before: `domain/` + `application/` is the write side,
`read/` only ever gets touched by `@TransactionalEventListener` methods
reacting to committed events — controllers in `api/` that serve queries talk
to `read/` tables directly and never load an aggregate.

---

## 14. Testing Notes Specific to Spring

- Use **Testcontainers** (`org.testcontainers:postgresql`) for integration
  tests rather than mocking `JdbcTemplate` — the whole point of this project
  is exercising real transactional and concurrency behavior, which an H2
  in-memory swap-in won't faithfully reproduce (especially the
  unique-constraint-triggered concurrency errors).
- `@TransactionalEventListener(phase = AFTER_COMMIT)` **will not fire** in a
  test wrapped in `@Transactional` with the default rollback-after-test
  behavior, since the transaction never actually commits. Use
  `@Commit`/`TestTransaction.flagForCommit()` or drop `@Transactional` on
  those specific test classes and clean up manually in `@AfterEach`.
