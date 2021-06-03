package intensiveteamhslee;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="serviceCenters", path="serviceCenters")
public interface ServiceCenterRepository extends PagingAndSortingRepository<ServiceCenter, Long>{


}
