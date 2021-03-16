/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package istio.fake.openfake;

/**
 * @author Spencer Gibb
 * @author Erik Kringen
 */
public class DefaultTargeter implements Targeter {

	@Override
	public <T> T target(FakeClientFactoryBean factory, Fake.Builder fake,
			FakeContext context, Target.HardCodedTarget<T> target) {

		return fake.target(target);
	}



}
