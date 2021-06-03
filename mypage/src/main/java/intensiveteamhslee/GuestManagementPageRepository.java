package intensiveteamhslee;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GuestManagementPageRepository extends CrudRepository<GuestManagementPage, Long> {

    List<GuestManagementPage> findByRoomId(Long roomId);

}