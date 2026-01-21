package requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import util.Service;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private String correlationId;
    private String message;
    private RequestType requestType;
    private Set<Service> services;
    private Map<String, Object> payload;

    @Override
    public String toString() {
        return "Request: " + "correlationId: " + correlationId + " message: " + message + " RequestType: " + requestType;
    }
}
