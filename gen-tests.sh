#!/usr/bin/env bash
set -euo pipefail

MAIN_ROOT="src/main/java"
TEST_ROOT="src/test/java"

# Create test root if missing
mkdir -p "$TEST_ROOT"

# Generate tests for classes we usually want to test
# (skip DTOs, Mappers, Configs, Application class, etc.)
find "$MAIN_ROOT" -type f -name "*.java" \
  | grep -Ev '/dto/|/mapper/|/config/|/exception/|/model/|/entity/|Application\.java$' \
  | while read -r file; do
      rel="${file#$MAIN_ROOT/}"                     # com/foo/bar/Thing.java
      pkg_dir="$(dirname "$rel")"                   # com/foo/bar
      class_name="$(basename "$rel" .java)"         # Thing
      test_class="${class_name}Test.java"           # ThingTest.java

      # only create tests for Controllers/Services/Repositories by name
      if [[ "$class_name" =~ (Controller|Service|Repository)$ ]]; then
        mkdir -p "$TEST_ROOT/$pkg_dir"
        test_path="$TEST_ROOT/$pkg_dir/$test_class"
        if [[ ! -f "$test_path" ]]; then
          package_name="$(echo "$pkg_dir" | tr '/' '.')"
          # choose a reasonable base template
          if [[ "$class_name" =~ Controller$ ]]; then
            cat > "$test_path" <<TEMPL
package $package_name;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest($class_name.class)
@AutoConfigureMockMvc(addFilters = false) // skip JwtFilter
class ${class_name}Test {

  @Autowired MockMvc mvc;

  // @MockBean any collaborators/services used by the controller
  // e.g. @MockBean AdminService adminService;

  @Test
  void ping_shouldReturn200() throws Exception {
    mvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }
}
TEMPL
          elif [[ "$class_name" =~ Service$ ]]; then
            cat > "$test_path" <<TEMPL
package $package_name;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ${class_name}Test {
  @Test
  void placeholder() {
    assertTrue(true);
  }
}
TEMPL
          else # Repository
            cat > "$test_path" <<TEMPL
package $package_name;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ${class_name}Test {
  @Test
  void contextLoads() { }
}
TEMPL
          fi
          echo "Created $test_path"
        else
          echo "Skip (exists) $test_path"
        fi
      fi
    done

echo "Done."
