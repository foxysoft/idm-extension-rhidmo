/*******************************************************************************
 * Copyright 2022 Lambert Giese
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
-- Surrogate definition of idmv_value_basic for unit tests based on H2 database.
-- Only those columns accessed by Rhidmo are included. Datatypes are specific to H2.
CREATE TABLE idmv_value_basic (
	MSKEY int,
	IS_ID int,
	AttrName varchar(200),
	aValue nvarchar(2000)
);
