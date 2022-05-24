-- Surrogate definition of idmv_value_basic for unit tests based on H2 database.
-- Only those columns accessed by Rhidmo are included. Datatypes are specific to H2.
CREATE TABLE idmv_value_basic (
	MSKEY int,
	IS_ID int,
	AttrName varchar(200),
	aValue nvarchar(2000)
);
