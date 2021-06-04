package intensiveteamhslee;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import feign.Param;


@RepositoryRestResource(collectionResourceRel="marketings", path="marketings")
public interface MarketingRepository extends PagingAndSortingRepository<Marketing, Long>{
    Marketing findByRoomId(@Param("roomId") Long roomId);
}
