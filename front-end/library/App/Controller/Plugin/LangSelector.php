<?php
/**
 * @author      Danny Froberg <danny@hackix.com>
 * @name        App_Controller_Plugin_LangSelector
 * @filesource  library/App/Controller/Plugin/LangSelector.php
 * @tutorial    Instantiate in application.ini with;
 *              resources.frontController.plugins.LangSelector =
 *              "App_Controller_Plugin_LangSelector"
 * @desc        Takes the lang parameneter when set either via a
 *              route or get/post and switches Locale, This depends
 *              on the main initTranslate function in Bootstrap.php
 *              to set the initial Zend_Translate object.
 *              Inspiration from ZendCasts LangSelector.
 */
class App_Controller_Plugin_LangSelector extends Zend_Controller_Plugin_Abstract
{
    public function preDispatch(Zend_Controller_Request_Abstract $request)
    {
        $registry = Zend_Registry::getInstance();
        // Get our translate object from registry.
        $translate = $registry->get('Zend_Translate');
        $currLocale = $translate->getLocale();
        // Create Session block and save the locale
        $session = new Zend_Session_Namespace('session');

        $lang = $request->getParam('lang','');
        // Register all your "approved" locales below.
        switch($lang) {
            case "sv":
                $langLocale = 'sv_SE'; break;
            case "fr":
                $langLocale = 'fr_FR'; break;
            case "en":
                $langLocale = 'en_US'; break;
            default:
                /**
                 * Get a previously set locale from session or set
                 * the current application wide locale (set in
                 * Bootstrap)if not.
                 */
                $langLocale = isset($session->lang) ? $session->lang : $currLocale;
        }

        $newLocale = new Zend_Locale();
        $newLocale->setLocale($langLocale);
        $registry->set('Zend_Locale', $newLocale);

        $translate->setLocale($langLocale);
        $session->lang = $langLocale;

        // Save the modified translate back to registry
        $registry->set('Zend_Translate', $translate);
    }
}