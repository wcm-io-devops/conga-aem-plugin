package io.wcm.devops.conga.plugins.aem.it.aem65.testcontext;

import static com.adobe.cq.wcm.core.components.testing.mock.ContextPlugins.CORE_COMPONENTS;
import static io.wcm.testing.mock.wcmio.caconfig.ContextPlugins.WCMIO_CACONFIG;
import static io.wcm.testing.mock.wcmio.handler.ContextPlugins.WCMIO_HANDLER;
import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static io.wcm.testing.mock.wcmio.wcm.ContextPlugins.WCMIO_WCM;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;

import java.io.IOException;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.wcmio.caconfig.MockCAConfig;

import io.wcm.devops.conga.plugins.aem.it.aem65.config.AppTemplate;
import io.wcm.devops.conga.plugins.aem.it.aem65.config.impl.LinkHandlerConfigImpl;
import io.wcm.devops.conga.plugins.aem.it.aem65.config.impl.MediaFormatProviderImpl;
import io.wcm.devops.conga.plugins.aem.it.aem65.config.impl.MediaHandlerConfigImpl;

/**
 * Sets up {@link AemContext} for unit tests in this application.
 */
public final class AppAemContext {

  private AppAemContext() {
    // static methods only
  }

  /**
   * @return {@link AemContext}
   */
  public static AemContext newAemContext() {
    return newAemContextBuilder().build();
  }

  /**
   * @return {@link AemContextBuilder}
   */
  public static AemContextBuilder newAemContextBuilder() {
    return newAemContextBuilder(ResourceResolverType.RESOURCERESOLVER_MOCK);
  }

  /**
   * @return {@link AemContextBuilder}
   */
  public static AemContextBuilder newAemContextBuilder(@NotNull ResourceResolverType resourceResolverType) {
    return new AemContextBuilder(resourceResolverType)
        .plugin(CACONFIG)
        .plugin(CORE_COMPONENTS, WCMIO_SLING, WCMIO_WCM, WCMIO_CACONFIG, WCMIO_HANDLER)
        .afterSetUp(SETUP_CALLBACK);
  }

  /**
   * Custom set up rules required in all unit tests.
   */
  private static final AemContextCallback SETUP_CALLBACK = new AemContextCallback() {
    @Override
    public void execute(@NotNull AemContext context) throws PersistenceException, IOException {

      // context path strategy
      MockCAConfig.contextPathStrategyRootTemplate(context, AppTemplate.HOMEPAGE.getTemplatePath());

      // setup handler
      context.registerInjectActivateService(LinkHandlerConfigImpl.class);
      context.registerInjectActivateService(MediaHandlerConfigImpl.class);
      context.registerInjectActivateService(MediaFormatProviderImpl.class);

    }
  };

}
