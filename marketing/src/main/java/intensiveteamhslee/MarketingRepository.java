package intensiveteamhslee;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="marketings", path="marketings")
public interface MarketingRepository extends PagingAndSortingRepository<Marketing, Long>{


}
