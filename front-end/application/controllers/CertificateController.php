<?php

require_once APPLICATION_PATH . '/managers/CertificateManager.class.php';
require_once APPLICATION_PATH . '/models/Cert.class.php';
require_once APPLICATION_PATH . '/models/SSLCRL.class.php';
require_once APPLICATION_PATH . '/models/SSLKey.class.php';

class CertificateController extends Zend_Controller_Action{

    private $certificateManager;
    private $keyList;
    private $caList;
    private $crlList;

    public function init()
    {
        /* Initialize action controller here */
        $this->certificateManager = new CertificateManager();
    }

    public function indexAction()
    {
        $messenger = $this->_helper->getHelper('FlashMessenger');
        $this->view->messages = $messenger->getMessages();
        $this->keyList = $this->certificateManager->getAllKeys();
        $this->view->keys = $this->keyList;
        $this->caList = $this->certificateManager->getAllCAs();
        $this->view->cas = $this->caList;
        $this->crlList = $this->certificateManager->getAllCRLs();
        $this->view->crls = $this->crlList;
    }

    public function keyAction()
    {
        $id = $this->_getParam("id");
        $request = $this->getRequest();
        $isPost = $request->isPost();

        //print_r("Got id: ".$id);
        if(empty($id) || (($id === "create") && !$isPost)) {
            $this->keyNewAction();
        } else if ($isPost) {
            $this->keyPostAction(($id === "create") ? null : $id);
        } else {
            $this->keyEditAction($id);
        }
    }

    private function keyNewAction()
    {
        $this->view->isNew = true;
        $this->view->key = null;
        $this->view->cert = null;
    }

    private function keyEditAction($id)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $validationErrors = array();
        $cert = null;
        $key = $this->certificateManager->getKey($id);
        if ($key == null) {
            $error = CertificateManager::error();
            if (empty($error)) {
                $error = $translate->translate("Unable to retrieve key ").$id;
            }
            $validationErrors['default'] = $error;
        } else {
            $certId = $key->getActiveCert();
            if (!empty($certId)) {
                $cert = $this->certificateManager->getCert($key->getId(), $certId);
            }
            // For debugging ....
            /*
            $certs = $this->certificateManager->getAllCerts($key->getId());
            foreach ($certs as $keyCert) {
                print_r($keyCert->getDisplayName());
            }
            */
        }
        $this->view->isNew = false;
        $this->view->validationErrors = $validationErrors;
        $this->view->key = $key;
        $this->view->cert = $cert;
    }

    private function keyPostAction($id)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        //print_r($_POST);
        $success = false;
        $validationErrors = array();
        $key = $this->validateFormAndGetKey($validationErrors);
        $cert = $this->validateFormAndGetCert($validationErrors);
        $newKey = empty($id);
        $certId = empty($cert) ? null : $cert->getId();
        $newCert = empty($certId);

        if (count($validationErrors) == 0) {
            $success = $newKey ? $this->certificateManager->createKey($key) : $this->certificateManager->updateKey($key);
            if (!$success)  {
                $operation = $newKey ? "creating" : "updating";
                $validationErrors['default'] = $translate->translate("Error ").$operation." key: ".CertificateManager::error();
            } else {
                if (!empty($cert)) {
                    $success = $newCert ? $this->certificateManager->createCert($key->getId(), $cert) : $this->certificateManager->updateCert($key->getId(), $cert);
                    if (!$success)  {
                        $operation = $newCert ? "creating" : "updating";
                        $validationErrors['default'] = $translate->translate("Error ").$operation." certificate: ".CertificateManager::error();
                    } else {
                        $certId = $cert->getId();
                    }
                }
                if ($success) {
                    // Set this cert to be the active cert for the key
                    // Must set content (key data) to null, since update will fail if content is non-null
                    $key->setContent(null);
                    $key->setActiveCert($certId == null ? "" : $certId);
                    $success = $this->certificateManager->updateKey($key);
                    if (!$success)  {
                        $validationErrors['default'] = $translate->translate("Couldn't update key with new active certificate: ").CertificateManager::error();
                    }
                }
            }
        }

        if ($success) {
            $messenger = $this->_helper->getHelper('FlashMessenger');
            $messenger->addMessage($newKey ? $translate->translate("Successfully Created Key") : $translate->translate("Successfully Updated Key"));
            $this->_redirect("/certificate");
        } else {
            $this->view->isNew = $newKey;
            $this->view->validationErrors = $validationErrors;
            $this->view->key = $key;
            $this->view->cert = $cert;
        }
    }

    /**
     * Handle the delete-Key action
     */
    public function keydeleteAction()
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        // Do we need to delete certs associated with this key, or does server do so?
        if ($this->certificateManager->deleteKey($this->_getParam("id"))) {
            $messenger = $this->_helper->getHelper('FlashMessenger');
            $messenger->addMessage($translate->translate("Successfully Deleted Key"));
        }
        $this->_redirect("/certificate");
    }

    private function validateFormAndGetKey(&$validationErrors)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $validationErrors = array();
        $key = new SSLKey();
        $validate_alnum_wspace = new Zend_Validate_Alnum(array('allowWhiteSpace' => true));

        $name = $_POST['key_name'];
        if (!$validate_alnum_wspace->isValid($name)) {
            $validationErrors['key_name'] = $translate->translate("The key name must be only alpha-numeric characters");
        }
        $key->setDisplayName($_POST['key_name']);

        // TODO: validate id value?
        $id = $_POST['key_id'];
        $key->setId($id);

        // Get key file contents
        if (isset($_FILES['key_file']) && !empty($_FILES['key_file']['name'])) {
            if (!$_FILES['key_file']['error']) {
                $contents = file_get_contents($_FILES['key_file']['tmp_name']);
                if ($contents !== false) {
                    $key->setContent($contents);
                } else {
                    $validationErrors['key_file'] = $translate->translate("There was an error getting contents of Key file.");
                }
            } else {
                $validationErrors['key_file'] = $translate->translate("There was an error uploading file: ").$_FILES['key_file']['error'];
            }
        } else if (empty($id)) {
            // A create operation (new SSLKey) must provide a content file
            $validationErrors['key_file'] = $translate->translate("Please upload a key file.");
        }

        return $key;
    }

    private function validateFormAndGetCert(&$validationErrors)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $cert = null;
        $validate_alnum_wspace = new Zend_Validate_Alnum(array('allowWhiteSpace' => true));

        $nameSet = isset($_POST['cert_name']) && !empty($_POST['cert_name']);
        $fileSet = isset($_FILES['cert_file']) && !empty($_FILES['cert_file']['name']);

        if ($nameSet || $fileSet) {
            $cert = new Cert();

            // TODO: validate id value?
            $certId = isset($_POST['cert_id']) ? $_POST['cert_id'] : null;
            $cert->setId($certId);

            $name =  $nameSet ? $_POST['cert_name'] : null;
            if (!$validate_alnum_wspace->isValid($name)) {
                $validationErrors['cert_name'] = $translate->translate("The certificate name must be only alpha-numeric characters");
            }
            $cert->setDisplayName($name);

            // Get cert file contents
            if ($fileSet) {
                if (!$_FILES['cert_file']['error']) {
                    $contents = file_get_contents($_FILES['cert_file']['tmp_name']);
                    if ($contents !== false) {
                        $cert->setContent($contents);
                    } else {
                        $validationErrors['cert_file'] = $translate->translate("There was an error getting contents of Certificate file.");
                    }
                } else {
                    $validationErrors['cert_file'] = $translate->translate("There was an error uploading file: ").$_FILES['cert_file']['error'];
                }
            } else if (empty($certId)) {
                // If the user has specified a certificate name, they must also specify a contents file
                $validationErrors['cert_file'] = $translate->translate("Please upload a certificate file.");
            }
        }

        return $cert;
    }

    public function caAction()
    {
        $id = $this->_getParam("id");
        $request = $this->getRequest();
        $isPost = $request->isPost();

        //print_r("Got id: ".$id);
        if(empty($id) || (($id === "create") && !$isPost)) {
            $this->caNewAction();
        } else if ($isPost) {
            $this->caPostAction(($id === "create") ? null : $id);
        } else {
            $this->caEditAction($id);
        }
    }

    private function caNewAction()
    {
        $this->view->isNew = true;
        $this->view->ca = null;
    }

    private function caEditAction($id)
    {
        $validationErrors = array();
        $ca = $this->certificateManager->getCA($id);
        if ($ca == null) {
            $error = CertificateManager::error();
            if (empty($error)) {
                $error = "Unable to retrieve ca ".$id;
            }
            $validationErrors['default'] = $error;
        }
        $this->view->isNew = false;
        $this->view->validationErrors = $validationErrors;
        $this->view->ca = $ca;
    }

    private function caPostAction($id)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        //print_r($_POST);
        $success = false;
        $validationErrors = array();
        $ca = $this->validateFormAndGetCA($validationErrors);
        $newCA = empty($id);
        if (count($validationErrors) == 0) {
            $success = $newCA ? $this->certificateManager->createCA($ca) : $this->certificateManager->updateCA($ca);
            if (!$success)  {
                $operation = $newCA ? "creating" : "updating";
                $validationErrors['default'] = $translate->translate("Error ").$operation." CA: ".CertificateManager::error();
            }
        }
        if ($success) {
            $messenger = $this->_helper->getHelper('FlashMessenger');
            $messenger->addMessage($newCA ? $translate->translate("Successfully Created Certificate Authority") : $translate->translate("Successfully Updated Certificate Authority"));
            $this->_redirect("/certificate");
        } else {
            $this->view->isNew = $newCA;
            $this->view->validationErrors = $validationErrors;
            $this->view->ca = $ca;
        }
    }

    /**
     * Handle the delete-CA action
     */
    public function cadeleteAction()
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        if ($this->certificateManager->deleteCA($this->_getParam("id"))) {
            $messenger = $this->_helper->getHelper('FlashMessenger');
            $messenger->addMessage($translate->translate("Successfully Deleted Certificate Authority"));
        }
        $this->_redirect("/certificate");
    }

    private function validateFormAndGetCA(&$validationErrors)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $validationErrors = array();
        $ca = new Cert();
        $validate_alnum_wspace = new Zend_Validate_Alnum(array('allowWhiteSpace' => true));

        // TODO: validate id field?
        $id = $_POST['ca_id'];
        $ca->setId($id);

        $name = $_POST['ca_name'];
        if (!$validate_alnum_wspace->isValid($name)) {
            $validationErrors['ca_name'] = $translate->translate("The CA name must be only alpha-numeric characters");
        }
        $ca->setDisplayName($_POST['ca_name']);

        if (isset($_FILES['ca_file']) && !empty($_FILES['ca_file']['name'])) {
            if (!$_FILES['ca_file']['error']) {
                $contents = file_get_contents($_FILES['ca_file']['tmp_name']);
                if ($contents !== false) {
                    $ca->setContent($contents);
                } else {
                    $validationErrors['ca_file'] = $translate->translate("There was an error getting contents of CA file.");
                }
            } else {
                $validationErrors['ca_file'] = $translate->translate("There was an error uploading file: ").$_FILES['content']['error'];
            }
        } else if (empty($id))  {
            $validationErrors['ca_file'] = $translate->translate("Please upload a CA file.");
        }
        return $ca;
    }

    public function crlAction()
    {
        $id = $this->_getParam("id");
        $request = $this->getRequest();
        $isPost = $request->isPost();

        //print_r("Got id: ".$id);
        if(empty($id) || (($id === "create") && !$isPost)) {
            $this->crlNewAction();
        } else if ($isPost) {
            $this->crlPostAction(($id === "create") ? null : $id);
        } else {
            $this->crlEditAction($id);
        }
    }

    private function crlNewAction()
    {
        $this->view->isNew = true;
        $this->view->crl = null;
    }

    private function crlEditAction($id)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $validationErrors = array();
        $crl = $this->certificateManager->getCRL($id);
        if ($crl == null) {
            $error = CertificateManager::error();
            if (empty($error)) {
                $error = $translate->translate("Unable to retrieve CRL ").$id;
            }
            $validationErrors['default'] = $error;
        }
        $this->view->isNew = false;
        $this->view->validationErrors = $validationErrors;
        $this->view->crl = $crl;
    }

    private function crlPostAction($id)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        //print_r($_POST);
        $success = false;
        $validationErrors = array();
        $newCRL = empty($id);

        $crl = $this->validateFormAndGetCRL($validationErrors);
        if (count($validationErrors) == 0) {
            $success = $newCRL ? $this->certificateManager->createCRL($crl) : $this->certificateManager->updateCRL($crl);
            if (!$success)  {
                $operation = $newCRL ? "creating" : "updating";
                $validationErrors['default'] = $translate->translate("Error ").$operation." CRL: ".CertificateManager::error();
            }
        }
        if ($success) {
            $messenger = $this->_helper->getHelper('FlashMessenger');
            $messenger->addMessage($newCRL ? $translate->translate("Successfully Created Certificate Revocation List") : $translate->translate("Successfully Updated Certificate Revocation List"));
            $this->_redirect("/certificate");
        } else {
            $this->view->isNew = $newCRL;
            $this->view->validationErrors = $validationErrors;
            $this->view->crl = $crl;
        }
    }

    /**
     * Handle the delete-CRL action
     */
    public function crldeleteAction()
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        if ($this->certificateManager->deleteCRL($this->_getParam("id"))) {
            $messenger = $this->_helper->getHelper('FlashMessenger');
            $messenger->addMessage($translate->translate("Successfully Deleted Certificate Revocation List"));
        }
        $this->_redirect("/certificate");
    }

    private function validateFormAndGetCRL(&$validationErrors)
    {
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $validationErrors = array();
        $crl = new SSLCRL();
        $validate_alnum_wspace = new Zend_Validate_Alnum(array('allowWhiteSpace' => true));

        // TODO: validate id field?
        $id = $_POST['crl_id'];
        $crl->setId($id);

        $name = $_POST['crl_name'];
        if (!$validate_alnum_wspace->isValid($name)) {
            $validationErrors['crl_name'] = $translate->translate("The CRL name must be only alpha-numeric characters");
        }
        $crl->setDisplayName($_POST['crl_name']);

        if (isset($_FILES['crl_file']) && !empty($_FILES['crl_file']['name'])) {
            if (!$_FILES['crl_file']['error']) {
                $contents = file_get_contents($_FILES['crl_file']['tmp_name']);
                if ($contents !== false) {
                    $crl->setContent($contents);
                } else {
                    $validationErrors['crl_file'] = $translate->translate("There was an error getting contents of CRL file.");
                }
            } else {
                $validationErrors['crl_file'] = $translate->translate("There was an error uploading file: ").$_FILES['content']['error'];
            }
        } else if (empty($id)) {
            $validationErrors['crl_file'] = $translate->translate("Please upload a CRL file.");
        }
        return $crl;
    }

}