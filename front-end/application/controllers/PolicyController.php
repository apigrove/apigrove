<?php
/**
 * Copyright © 2012 Alcatel-Lucent.
 *
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership. Licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 */
/**
 * Controller to handle all Policy-related operations
 *
 * Date: 8/22/12
 */

require_once APPLICATION_PATH . '/managers/PolicyManager.class.php';
require_once APPLICATION_PATH . '/models/Counter.class.php';
require_once APPLICATION_PATH . '/models/Context.class.php';
require_once APPLICATION_PATH . '/models/Policy.class.php';
require_once APPLICATION_PATH . '/models/Api.class.php';
require_once APPLICATION_PATH . '/models/Auth.class.php';
require_once "SharedViewUtility.php";

class PolicyController extends Zend_Controller_Action{

    /**
     * @var PolicyManager $policyManager
     */
    private $policyManager;
    private $apiList;
    private $authList;

    public function init()
    {
        /* Initialize action controller here */
        $this->policyManager = new PolicyManager();
    }

    /**
     * Function to handle index request on /policy
     * Fills out the policy-list view with policies, apis and auths
     * retrieved from E3.
     */
    public function indexAction()
    {
        //        //print_r("indexAction");
        $messenger = $this->_helper->getHelper('FlashMessenger');
        $this->view->messages = $messenger->getMessages();
        $this->apiList = $this->policyManager->getAllApis();
        $this->view->apis = $this->apiList;
        $this->authList = $this->policyManager->getAllAuths();
        $this->view->auths = $this->authList;
        $this->view->policies = $this->policyManager->getAllPolicies(true);
    }

    /**
     * General entry point for detail-view processing
     * Until we have a Flow object to handle state, infer state from param and method
     *
     * Four cases:
     *      1. empty($id) or $id === "create", GET method: new Policy entry (empty form)
     *      2. $id === "create", POST method: submit form contents as new Policy to E3
     *      3. !empty($id) && $id !== "create",  POST method: submit updated Policy to E3
     *      4. otherwise: treat $id as Policy-id, load from E3 into form for editing
     */
    public function formAction()
    {
        $id = $this->_getParam("id");
        $request = $this->getRequest();
        $isPost = $request->isPost();

        //print_r("Got id: ".$id);
        if(empty($id) || (($id === "create") && !$isPost)) {
            $this->newAction();
        } else if (($id === "create") && $isPost) {
            $this->createAction();
        } else if ($isPost) {
            $this->updateAction($id);
        } else {
            $this->editAction($id);
        }
    }

    /**
     * Handle the create-new request on the Policy detail form
     */
    public function newAction()
    {
        $this->view->isNew = true;
        $this->loadPolicy(null);
    }

    /**
     * Handle the edit-existing operation for the Policy detail form
     *
     * @param $policyId The policy-id used to retrieve a Policy from E3
     */
    public function editAction($policyId)
    {
        $validationErrors = array();
        $policy = $this->policyManager->getPolicy($policyId);
        if ($policy == null) {
            $error = PolicyManager::error();
            if (empty($error)) {
                $error = "Unable to retrieve policy ".$policyId;
            }
            $validationErrors['default'] = $error;
        }
        $this->view->isNew = false;
        $this->view->validationErrors = $validationErrors;
        $this->loadPolicy($policy);
    }

    /**
     * Handle the post-new Policy operation on the Policy detail form
     */
    public function createAction()
    {
        $success = false;
        $validationErrors = array();
        $policy = $this->validateFormAndGetPolicy(null, $validationErrors);
        if (count($validationErrors) == 0) {
            if ($this->policyManager->createPolicy($policy)) {
                $success = true;
            } else {
                $validationErrors['default'] = "Error creating policy: ".PolicyManager::error();
            }
        }
        if ($success) {
            $messenger = $this->_helper->getHelper('FlashMessenger');
            $messenger->addMessage("Successfully Created Policy");
            $this->_redirect("/policy");
        } else {
            $this->view->isNew = true;
            $this->view->validationErrors = $validationErrors;
            $this->loadPolicy($policy);
        }
    }

    /**
     * Handle the post-update Policy operation on the Policy detail form
     *
     * @param $policyId The id of the Policy to update
     */
    public function updateAction($policyId)
    {
        $success = false;
        $validationErrors = array();
        //print_r($_POST);
        $policy = $this->validateFormAndGetPolicy($policyId, $validationErrors);
        if (count($validationErrors) == 0) {
            if ($this->policyManager->updatePolicy($policy)) {
                $this->view->flashMessage = "Policy updated!";
                $success = true;
            } else {
                $validationErrors['default'] = "Error updating policy: ".PolicyManager::error();
            }
        }
        if ($success) {
            $messenger = $this->_helper->getHelper('FlashMessenger');
            $messenger->addMessage("Successfully Updated Policy");
            $this->_redirect("/policy");
        } else {
            $this->view->isNew = false;
            $this->view->validationErrors = $validationErrors;
            $this->loadPolicy($policy);
        }
    }

    /**
     * Handle the delete-Policy action
     */
    public function deleteAction()
    {
        $this->policyManager->deletePolicy($this->_getParam("id"));
        $this->_redirect("/policy");
    }

    /**
     * Loads the Policy detail form with the contents of the
     * Policy specified as well as the Api and Auth lists
     * via a new request to E3
     *
     * @param $policy   The Policy object to fill in the form
     */
    private function loadPolicy($policy)
    {
        $this->loadApiAndAuthLists();
        $this->view->policy = $policy;
    }

    /**
     * Executes new calls to get Api and Auth lists from E3
     * and assigns the lists to member variables
     */
    private function loadApiAndAuthLists()
    {
        $this->apiList = $this->policyManager->getAllApis(true);
        $this->authList = $this->policyManager->getAllAuths(true);

        $this->view->apis = $this->apiList;
        $this->view->auths = $this->authList;
    }

    /**
     * Validates the fields in the Policy detail form and returns
     * a new Policy filled with values from the form.  If there are
     * no validation errors, count($validationErrors) will be 0.
     *
     * @param $policyId The id to use for the new Policy; if null, will use the value from the 'policy_id' field on the form
     * @param $validationErrors An array to use for holding any validation errors encountered; will be cleared on entry
     * @return Policy   A new Policy object filled with the contents of the form, regardless of any validation errors
     */
    private function validateFormAndGetPolicy($policyId, &$validationErrors)
    {
        $validationErrors = array();
        $policy = $this->validateStandardFieldsAndGetPolicy($policyId, $validationErrors);

        // Set the policy context by getting and validating rate fields
        $context = $this->validateRatesAndGetContext($validationErrors);
        if ($context != null) {
            $context->setId("pctx");
            $contexts[] = $context;
            $policy->setContexts($contexts);
        }
        $this->deserializeAndValidateSharedView($policy, $validationErrors);
        return $policy;
    }

    /**
     * Validates the fields in the Policy detail form that are common to all Policies,
     * and returns a new Policy filled in with those fields.  If there are no
     * validation errors, no entries will be added to $validationErrors.
     *
     * @param $policyId The id to use for the new Policy; if null, will use the value from the 'policy_id' field on the form
     * @param $validationErrors An array to use for holding any validation errors encountered (not cleared on entry)
     * @return Policy   A new Policy object filled with the contents of the validated fields, regardless of any validation errors
     */
    private function validateStandardFieldsAndGetPolicy($policyId, &$validationErrors)
    {
        $policy = new Policy();

        $validate_alnum = new Zend_Validate_Alnum();
        //$validate_id_regex = new Zend_Validate_Regex(array('pattern' => '/^[a-z0-9A-Z-_ ]{1,256}$/'));
        //$validate_id_len = new Zend_Validate_StringLength(array('min' => 1, 'max' => 256));
        if (empty($policyId)) {
            $policyId = $_POST['policy_id'];
        }

        $success = true;
        if (!$validate_alnum->isValid($policyId)) {
            $validationErrors['policyId'] = "The policy ID must be only alpha-numeric characters";
            $success = false;
        }
        $policy->setId($policyId);
        if (isset($_POST['selected_api'])) {
            $policy->setApiIds($this->getSelectedApis());
        }

        $policy->setAuthIds($this->getSelectedAuths($_POST['auth_bucket_id']));

        return $policy;
    }

    /**
     * Validates the rate-limit fields on the Policy detail form and returns
     * a new Context value filled with a Counter instance for each specified limit or quota.
     * If there are no validation errors, no entries will be added to $validationErrors.
     *
     * @param $validationErrors An array to use for holding any validation errors encountered (not cleared on entry)
     * @return Context  A new Context instance with a Counter for each specified rate or quota
     */
    private function validateRatesAndGetContext(&$validationErrors)
    {
        $context = $this->getDefaultContext();
        $counter = $this->validateRateAndGetCounterForPeriod('per_second', $validationErrors);
        if ($counter->getThreshold() > 0 || $counter->getWarning() > 0) {
            $context->setRateLimitPerSecond($counter);
        }
        $counter = $this->validateRateAndGetCounterForPeriod('per_minute', $validationErrors);
        if ($counter->getThreshold() > 0 || $counter->getWarning() > 0) {
            $context->setRateLimitPerMinute($counter);
        }
        $counter = $this->validateRateAndGetCounterForPeriod('per_day', $validationErrors);
        if ($counter->getThreshold() > 0 || $counter->getWarning() > 0) {
            $context->setQuotaPerDay($counter);
        }
        $counter = $this->validateRateAndGetCounterForPeriod('per_week', $validationErrors);
        if ($counter->getThreshold() > 0 || $counter->getWarning() > 0) {
            $context->setQuotaPerWeek($counter);
        }
        $counter = $this->validateRateAndGetCounterForPeriod('per_month', $validationErrors);
        if ($counter->getThreshold() > 0 || $counter->getWarning() > 0) {
            $context->setQuotaPerMonth($counter);
        }
        return $context;
    }

    /**
     * Validates the form fields for a specific rate-limit quota.  Returns a new
     * Counter instance, which will be empty if no limit values are specified on the form.
     * If there are no validation errors, no entries will be added to $validationErrors.
     *
     * @param $quotaName    One of the quota values on the form, such as 'per_second' or 'per_day'
     * @param $validationErrors An array to use for holding any validation errors encountered (not cleared on entry)
     * @return Counter  A new Counter instance with quota values, whether or not there are validation errors
     */
    private function validateRateAndGetCounterForPeriod($quotaName, &$validationErrors)
    {
        $counter = new Counter();
        $success = true;

        $validate_int = new Zend_Validate_Int();
        $validate_gt_0 = new Zend_Validate_GreaterThan(array('min' => 0));
        $validate_gte_0 = new Zend_Validate_GreaterThan(array('min' => -1));

        // Validation based on (Drupal-based UI) code from e3_ui_policy_add_context_submit_validate
        if (!empty($_POST[$quotaName]['warning']) || !empty($_POST[$quotaName]['threshold'])) {
            $counter->setAction($_POST[$quotaName]['action']);
            //            $counter->setStatus($_POST[$quotaName]['status']);
            $threshold = !empty($_POST[$quotaName]['threshold']) ? trim($_POST[$quotaName]['threshold']) : "0";
            $warning = !empty($_POST[$quotaName]['warning']) ? trim($_POST[$quotaName]['warning']) : "0";
            $counter->setThreshold($threshold);
            $counter->setWarning($warning);

            if (strlen($threshold) > 0) {
                if (strlen($warning) == 0) {
                    $warning = 0;
                    $counter->setWarning("0");
                }
                if (!$validate_int->isValid($threshold) || !$validate_gt_0->isValid($threshold)) {
                    $validationErrors[$quotaName] = $this->displayNameForField($quotaName)." Threshold must be a positive integer if you want to include this Quota. ";
                    $success = false;
                } elseif (!$validate_int->isValid($warning) || !$validate_gte_0->isValid($warning)) {
                    $validationErrors[$quotaName] = $this->displayNameForField($quotaName)." Warning must be a non-negative integer. ";
                    $success = false;
                } elseif (Zend_Validate::is($warning, 'GreaterThan', array('min' => $threshold))) {
                    $validationErrors[$quotaName] = $this->displayNameForField($quotaName)." Warning may not be greater than the Threshold. ";
                    $success = false;
                }
            }
        }
        return $counter;
    }

    /**
     * Retrieve the header-transformation, property and tdr-rule values from the current form
     * request, validate them, and insert them into the specified Policy.
     * If there are no validation errors, no entries will be added to $validationErrors.
     *
     * @param $policy   The Policy to which the new values should be added
     * @param $validationErrors An array to use for holding any validation errors encountered (not cleared on entry)
     */
    private function deserializeAndValidateSharedView(&$policy, &$validationErrors)
    {
        $policy->headerTransformations = SharedViewUtility::deserializeHeaderTransformations($this->getRequest());
        $policy->properties = SharedViewUtility::deserializeProperties($this->getRequest());
        $policy->tdr = SharedViewUtility::deserializeTdrRules($this->getRequest());
        SharedViewUtility::validateHeaderTransformations($policy->headerTransformations, $validationErrors);
        SharedViewUtility::validateProperties($policy->properties, $validationErrors);
        SharedViewUtility::validateTdrRules($policy->tdr, $validationErrors);
    }

    /**
     * Creates and returns a new Context value for use with a Policy.  Assigns default
     * values to the Id and Status fields.
     *
     * @return Context  A new Context object with default values
     */
    private function getDefaultContext()
    {
        $context = new Context();
        $context->setId("1");   // arbitrary value?
        $context->setStatus("active");
        return $context;
    }

    /**
     * Retrieves the array of user-selected Apis from the latest POST request
     *
     * @return mixed    An array holding the selected Api Ids
     */
    private function getSelectedApis()
    {
        $selectedApis = $_POST['selected_api'];
        return $selectedApis;
    }

    /**
     * Retrieves an array with a single AuthIdsType value, which in turn
     * holds an array of selected Auth Ids from the latest POST request
     *
     * @return mixed    An array with an AuthIdsType instance with the array of selected Auth Ids
     */
    private function getSelectedAuths()
    {
        $selectedAuths = array();
        $authIdstype = new AuthIdsType();
        $authIdstype->authIds = @$_POST['selected_auth'];
        $authBucketId = isset($_POST['auth_bucket_id']) ? $_POST['auth_bucket_id'] : null;
        if (!empty($authBucketId)) {
            $authIdstype->setId($authBucketId);
        }
        $selectedAuths[] = $authIdstype;
        return $selectedAuths;
    }

    /**
     * Converts a name value from a form field to a value suitable for display.  Not all
     * form fields have specified display values; in those cases the original name is returned.
     *
     * @param $fieldName    The name value of a Policy detail form field
     * @return string   The display string for the field
     */
    private function displayNameForField($fieldName)
    {
        switch ($fieldName) {
            case 'per_second':
                return 'Per-Second';
            case 'per_minute':
                return 'Per-Minute';
            case 'per_hour':
                return 'Hourly';
            case 'per_day':
                return 'Daily';
            case 'per_week':
                return 'Weekly';
            case 'per_month':
                return 'Monthly';
        }
        return $fieldName;
    }

}

