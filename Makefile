artifact_name       := advanced-company-search-consumer
version             := unversioned

.PHONY: all
all: build

.PHONY: clean
clean:
	mvn clean

.PHONY: build
build:
	mvn compile

.PHONY: test
test: test-unit

.PHONY: test-unit
test-unit: clean
	mvn test -DexcludedGroups="integration-test"

.PHONY: test-integration
test-integration: clean
	mvn test -Dgroups="unit-test, integration-test"

.PHONY: package
package:
ifndef version
	$(error No version given. Aborting)
endif
	$(info Packaging version: $(version))
	mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	mvn package -DskipTests=true
	$(eval tmpdir:=$(shell mktemp -d build-XXXXXXXXXX))
	cp ./target/$(artifact_name)-$(version).jar $(tmpdir)/$(artifact_name).jar
	cd $(tmpdir); zip -r ../$(artifact_name)-$(version).zip *
	rm -rf $(tmpdir)

.PHONY: dist
dist: clean package

.PHONY: publish
publish:
	mvn jar:jar deploy:deploy

.PHONY: sonar
sonar:
	mvn sonar:sonar

.PHONY: sonar-pr-analysis
sonar-pr-analysis:
	mvn sonar:sonar -P sonar-pr-analysis

.PHONY: security-check
security-check:
	mvn org.owasp:dependency-check-maven:update-only
	mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=4 -DassemblyAnalyzerEnabled=false