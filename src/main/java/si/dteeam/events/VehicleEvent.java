package si.dteeam.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VehicleEvent extends ApplicationEvent {
    private final Long chatId;
    private final String message;

    public VehicleEvent(Object source, Long chatId, String message) {
        super(source);
        this.chatId = chatId;
        this.message = message;
    }

}
