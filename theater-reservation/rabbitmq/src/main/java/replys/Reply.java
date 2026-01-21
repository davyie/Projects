package replys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
    private String correlationId;
    private String message;
    private ReplyType replyType;
    private Map<String, Object> payload;
}
