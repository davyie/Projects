package replys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
    private String correlationId;
    private String message;
    private ReplyType replyType;
    private Object payload;
}
