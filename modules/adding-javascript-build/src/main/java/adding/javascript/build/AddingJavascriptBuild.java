package adding.javascript.build;
import java.util.function.Supplier;
import org.osgi.service.component.annotations.Component;
@Component(
	immediate = true,
	property = {
	},
	service = Supplier.class
)
public class AddingJavascriptBuild implements Supplier<String> {
}