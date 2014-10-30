package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp

import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
/**
 * Created by gerardin on 10/27/14.
 */
class TestCase10 extends TestCaseStrategy{


    TestCase10(TestCaseExecutor executor) {
        super(executor)
    }

    @Override
    UserMessage run(String tcid, Object userInput, String username) {

        //TODO tc10
        // store record in db
        // sends a direct message (Needs to provide a message id for correlation)
        // receive xdr (we can have one endpoint or create multiple)
        // validate also the content to make sure it matches the direct message

    }
}
