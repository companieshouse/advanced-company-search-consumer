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
test: test-unit test-integration

.PHONY: test-unit
test-unit: clean
	mvn clean verify -Dskip.unit.tests=false -Dskip.integration.tests=true

.PHONY: test-integration
test-integration: clean
	mvn clean verify -Dskip.unit.tests=true -Dskip.integration.tests=false

.PHONY: docker-image
docker-image: clean
	mvn package -Dskip.unit.tests=true -Dskip.integration.tests=true jib:dockerBuild

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


