package replys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import util.MessageStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
    private String correlationId;
    private String message;
    private ReplyType replyType;
    private Object payload;
    private MessageStatus messageStatus;

    @Override
    public String toString() {
        return "Reply: " + "correlationId: " + correlationId + " message: " + message + " ReplyType: " + replyType + " payload: " + payload.toString();
    }
}
