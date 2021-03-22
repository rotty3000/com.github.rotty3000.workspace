package adding.javascript.build;

import java.util.function.Supplier;

import org.osgi.service.component.annotations.Component;

/**
 * @author rotty
 */
@Component(
	immediate = true,
	property = {
		// TODO enter required service properties
	},
	service = Supplier.class
)
public class AddingJavascriptBuild implements Supplier {

	// TODO enter required service methods

}