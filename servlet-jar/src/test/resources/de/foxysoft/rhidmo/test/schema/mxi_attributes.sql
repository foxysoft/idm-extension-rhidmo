-- Surrogate definition of idmv_value_basic for unit tests based on H2 database.
-- Only those columns accessed by Rhidmo are included. Datatypes are specific to H2.
CREATE TABLE mxi_attributes (
	AttrName varchar(200),
	IS_ID int,
	MultiValue tinyint,
	ReferenceObjectClass int
);
