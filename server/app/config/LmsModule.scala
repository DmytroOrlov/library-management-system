package config

import com.google.inject.{Singleton, Provides, AbstractModule}
import com.typesafe.config.Config
import play.api.Configuration

class LmsModule extends AbstractModule {
  override def configure(): Unit = {}

  @Provides @Singleton
  def config(c: Configuration): Config = c.underlying
}
