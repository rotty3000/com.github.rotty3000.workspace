package adding.javascript.build;
import java.util.function.Supplier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
@Component(
	immediate = true,
	property = {
		"osgi.command.scope=ajb",
		"osgi.command.function=get"
	},
	service = Supplier.class
)
public class AddingJavascriptBuild implements Supplier<String> {
	@Override
	public String get() {
		return "Something interesting!";
	}
	@Activate
	void activate() {
		System.out.println("Activated!");
	}
	@Deactivate
	void deactivate() {
		System.out.println("Deactivated!");
	}
}