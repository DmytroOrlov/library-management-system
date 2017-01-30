import com.google.inject.{AbstractModule, Provides, Singleton}
import com.typesafe.config.Config
import play.api.Configuration

class Module extends AbstractModule {
  def configure() = ()

  @Provides @Singleton
  def config(c: Configuration): Config = c.underlying
}
