package intensiveteamhslee;

public class BookCanceledByService extends AbstractEvent {

    private Long id;

    public BookCanceledByService(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
