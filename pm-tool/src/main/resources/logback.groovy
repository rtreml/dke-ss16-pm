
statusListener(OnConsoleStatusListener)

def appenderList = ["CONSOLE"]

addInfo("Hostname is ${hostname}")

appender("CONSOLE", ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
	}
}

root(INFO, appenderList)
logger("org.springframework.jdbc.core", INFO)
logger("org.springframework.boot", TRACE)
logger("org.hsqldb", INFO)

logger("at.jku.dke", INFO)
logger("com.fasterxml", ERROR)

