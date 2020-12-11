import com.qlhl.bean.Role;
import com.qlhl.bean.User;
import com.qlhl.service.RoleService;
import com.qlhl.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author: ChenTong
 * @create 2020/12/9 15:26
 */

public class ShiroLogin {

    private ApplicationContext applicationContext;
    UserService userloginService;
    RoleService aa ;

    @Before
    public void setUp() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext(new String[]{"spring/applicationContext-dao.xml",
                "spring/applicationContext-service.xml"});
        userloginService = (UserService) applicationContext.getBean("userServiceImpl");
        aa = (RoleService) applicationContext.getBean("roleServiceImpl");
    }

    @Test
    public void findByName() throws Exception {
        User u = userloginService.findUser("admin");
        System.out.println(u);
    }

    @Test
    public void findByID(){
        Role u = aa.findRole(0);
        System.out.println(u);
    }

}
