package flapjack.auth;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * An hk2 service for the @Auth annotation
 *
 * @author Ray Vanderborght
 */
@Service @Singleton
public class AuthService implements InterceptionService {

	@Inject private MethodInterceptor auth;

	@Override
	public Filter getDescriptorFilter() {
		return BuilderHelper.allFilter();
	}

	@Override
	public List<ConstructorInterceptor> getConstructorInterceptors(Constructor<?> ctor) {
		return null;
	}

	@Override
	public List<MethodInterceptor> getMethodInterceptors(Method method) {
		if (method.isAnnotationPresent(Auth.class)) {
			return Collections.singletonList(auth);
		}
		return null;
	}
}
