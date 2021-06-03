package intensiveteamhslee;

import intensiveteamhslee.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired MarketingRepository marketingRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBooked_BookAdd(@Payload Booked booked){

        if(!booked.validate()) return;

        System.out.println("\n\n##### listener BookAdd : " + booked.toJson() + "\n\n");

        // Sample Logic //
        Marketing marketing = new Marketing();
        marketingRepository.save(marketing);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRoomRegistered_RoomAdd(@Payload RoomRegistered roomRegistered){

        if(!roomRegistered.validate()) return;

        System.out.println("\n\n##### listener RoomAdd : " + roomRegistered.toJson() + "\n\n");

        // Sample Logic //
        Marketing marketing = new Marketing();
        marketingRepository.save(marketing);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
