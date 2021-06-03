
package intensiveteamhslee.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="room", url="http://room:8080")
public interface RoomService {

    @RequestMapping(method= RequestMethod.GET, path="/rooms")
    public void deleteRoom(@RequestBody Room room);

}