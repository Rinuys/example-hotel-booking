
package intensiveteamhslee.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@FeignClient(name="book", url="http://book:8080")
public interface BookService {

    @RequestMapping(method= RequestMethod.DELETE, path="/books/{id}")
    public void bookCancel(@PathVariable("id") Long id);

}