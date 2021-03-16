/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package istio.fake.support;

import static istio.fake.support.PojoUtil.isUserPojo;
import static istio.fake.support.PojoUtil.toMap;
import static lombok.AccessLevel.PRIVATE;

import istio.fake.FakeException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PojoWriter extends AbstractWriter {

  Iterable<Writer> writers;

  @Override
  public boolean isApplicable (Object object) {
    return isUserPojo(object);
  }

  @Override
  public void write (Output output, String boundary, String key, Object object) throws FakeException {
    val map = toMap(object);
    for (val entry : map.entrySet()) {
      val writer = findApplicableWriter(entry.getValue());
      if (writer == null) {
        continue;
      }

      writer.write(output, boundary, entry.getKey(), entry.getValue());
    }
  }

  private Writer findApplicableWriter (Object value) {
    for (val writer : writers) {
      if (writer.isApplicable(value)) {
        return writer;
      }
    }
    return null;
  }
}
