
package intensiveteamhslee.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@FeignClient(name="room", url="http://room:8080")
public interface RoomService {

    @RequestMapping(method= RequestMethod.DELETE, path="/rooms/{id}")
    public void deleteRoom(@PathVariable("id") Long id);
    
}