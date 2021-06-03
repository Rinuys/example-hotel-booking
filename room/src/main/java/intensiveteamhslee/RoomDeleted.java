package intensiveteamhslee;

public class RoomDeleted extends AbstractEvent {

    private Long id;
    private Integer price;

    public RoomDeleted(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
