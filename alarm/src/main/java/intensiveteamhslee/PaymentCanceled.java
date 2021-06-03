package intensiveteamhslee;

public class PaymentCanceled extends AbstractEvent {

    private Long id;
    private Long bookId;
    private Long roomId;
    private Integer price;
    private Date startDate;
    private Date endDate;
    private String bookStatus;

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
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
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
        return bookStatus;
    }

    public void setStatus(String bookStatus) {
        this.bookStatus = bookStatus;
    }
}