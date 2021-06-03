package intensiveteamhslee;

import intensiveteamhslee.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Date;

@Service
public class GuestManagementPageViewHandler {


    @Autowired
    private GuestManagementPageRepository guestManagementPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentApproved_then_CREATE_1 (@Payload PaymentApproved paymentApproved) {
        try {

            if (!paymentApproved.validate()) return;

            // view 객체 생성
            GuestManagementPage guestManagementPage = new GuestManagementPage();
            // view 객체에 이벤트의 Value 를 set 함
            guestManagementPage.setBookId(paymentApproved.getBookId());
            guestManagementPage.setPrice(paymentApproved.getPrice());
            guestManagementPage.setStartDate(paymentApproved.getStartDate());
            guestManagementPage.setEndDate(paymentApproved.getEndDate());
            guestManagementPage.setRoomId(paymentApproved.getRoomId());
            guestManagementPage.setBookCount(0);
            // view 레파지 토리에 save
            guestManagementPageRepository.save(guestManagementPage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookCounted_then_UPDATE_1(@Payload BookCounted bookCounted) {
        try {
            if (!bookCounted.validate()) return;
                // view 객체 조회
            List<GuestManagementPage> guestManagementPageList = guestManagementPageRepository.findByRoomId(bookCounted.getRoomId());
            for(GuestManagementPage guestManagementPage : guestManagementPageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                guestManagementPage.setBookCount(bookCounted.getBookCount());
                // view 레파지 토리에 save
                guestManagementPageRepository.save(guestManagementPage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCanceled_then_DELETE_1(@Payload PaymentCanceled paymentCanceled) {
        try {
            if (!paymentCanceled.validate()) return;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}