
package intensiveteamhslee;

import java.util.Date;

public class BookCanceled extends AbstractEvent {

    private Long id;
    private Long roomId;
    private Date startDate;
    private Date endDate;
    private String status;
    private Integer price;

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

