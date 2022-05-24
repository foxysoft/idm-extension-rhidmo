-- Surrogate definition of mxp_tasks for unit tests based on H2 database.
-- Only those columns accessed by Rhidmo are included. Datatypes are specific to H2.
CREATE TABLE mxp_tasks (
	TaskID int,
	mcPackageID int,
	IDStore int
);
