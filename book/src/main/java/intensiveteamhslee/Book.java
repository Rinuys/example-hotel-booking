package intensiveteamhslee;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Book_table")
public class Book {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long roomId;
    private Date startDate;
    private Date endDate;
    private String status;
    private Integer price;

    @PostPersist
    public void onPostPersist(){
        Booked booked = new Booked();
        BeanUtils.copyProperties(this, booked);
        booked.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        intensiveteamhslee.external.Payment payment = new intensiveteamhslee.external.Payment();
        // mappings goes here
        payment.setBookId(booked.getId());
        payment.setBookStatus("BookPaied");
        payment.setEndDate(booked.getEndDate());
        payment.setStartDate(booked.getStartDate());
        payment.setRoomId(booked.getRoomId());
        payment.setPrice(booked.getPrice());
        BookApplication.applicationContext.getBean(intensiveteamhslee.external.PaymentService.class)
            .pay(payment);


    }

    @PostRemove
    public void onPostRemove(){
        BookCanceled bookCanceled = new BookCanceled();
        BeanUtils.copyProperties(this, bookCanceled);
        bookCanceled.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public void setPrice(Integer price){
        this.price = price;
    }

    public Integer getPrice(){
        return price;
    }



}
