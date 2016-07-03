/*
 * Copyright 2016 Yusuke Ikeda
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

package org.yukung.daguerreo.domain.entity;

import java.io.Serializable;

/**
 * Interface shown to have an ID for identifying the object.
 *
 * @author yukung
 */
public interface Identifiable<ID extends Serializable> extends Serializable {

    /**
     * Returns the ID of this entity.
     *
     * @return the ID
     */
    ID getId();
}
