package intensiveteamhslee;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="ServiceCenter_table")
public class ServiceCenter {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long bookId;
    private Long roomId;

    @PostPersist
    public void onPostPersist(){
        if(bookId != null){
            BookCanceledByService bookCanceledByService = new BookCanceledByService();
            BeanUtils.copyProperties(this, bookCanceledByService);
            bookCanceledByService.publishAfterCommit();

            //Following code causes dependency to external APIs
            // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

            intensiveteamhslee.external.Book book = new intensiveteamhslee.external.Book();
            // mappings goes here
            ServiceCenterApplication.applicationContext.getBean(intensiveteamhslee.external.BookService.class).
                bookCancel(bookId);;
        }

        if(roomId != null){
            RoomDeletedByService roomDeletedByService = new RoomDeletedByService();
            BeanUtils.copyProperties(this, roomDeletedByService);
            roomDeletedByService.publishAfterCommit();

            //Following code causes dependency to external APIs
            // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

            intensiveteamhslee.external.Room room = new intensiveteamhslee.external.Room();
            // mappings goes here
            ServiceCenterApplication.applicationContext.getBean(intensiveteamhslee.external.RoomService.class)
                .deleteRoom(roomId);
        }


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }




}
