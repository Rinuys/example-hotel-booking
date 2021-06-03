package intensiveteamhslee;

public class RoomDeletedByService extends AbstractEvent {

    private Long id;

    public RoomDeletedByService(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
