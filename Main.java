/**
 * 
 */
//package uk.leeds.csp.client;
import org.junit.Assert;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlObject;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;
import org.ogf.graap.wsag.api.AgreementOffer;
import org.ogf.graap.wsag.api.exceptions.NegotiationException;
import org.ogf.graap.wsag.api.exceptions.ResourceUnavailableException;
import org.ogf.graap.wsag.api.exceptions.ResourceUnknownException;
import org.ogf.graap.wsag.api.security.KeystoreLoginContext;
import org.ogf.graap.wsag.api.security.KeystoreProperties;
import org.ogf.graap.wsag.api.types.AgreementOfferType;
//import org.ogf.graap.wsag.client.api.AgreementClient;
//import org.ogf.graap.wsag.client.api.AgreementFactoryClient;
//import org.ogf.graap.wsag.client.api.AgreementFactoryRegistryClient;
//import org.ogf.graap.wsag.client.api.NegotiationClient;
import org.ogf.graap.wsag.client.api.*;
import org.ogf.graap.wsag.samples.actions.*;
import org.ogf.graap.wsag.samples.actions.SampleAgreementTemplate;
import org.ogf.graap.wsag.samples.actions.SampleNegotiationOffer;
import org.ogf.graap.wsag4j.types.scheduling.TimeConstraintDocument;
import org.ogf.graap.wsag4j.types.scheduling.TimeConstraintType;
import org.ogf.schemas.graap.wsAgreement.AgreementStateType;
import org.ogf.schemas.graap.wsAgreement.AgreementTemplateType;
import org.ogf.schemas.graap.wsAgreement.GuaranteeTermStateType;
import org.ogf.schemas.graap.wsAgreement.OfferItemType.ItemConstraint;
import org.ogf.schemas.graap.wsAgreement.ServiceDescriptionTermType;
import org.ogf.schemas.graap.wsAgreement.ServiceTermStateType;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationConstraintSectionType;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationContextDocument;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationContextType;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationOfferContextType;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationOfferItemType;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationOfferStateType;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationOfferType;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationRoleType;
import org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationType;
//import org.ogf.graap.wsag.security.core.*;
import org.w3.x2005.x08.addressing.EndpointReferenceType;


/**
 * @author adriano
 *
 */
@SuppressWarnings("deprecation")
public class Main {

	private static final Logger LOG = Logger.getLogger( Main.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			/*** LOADING AGREEMENT FACTORY CLIENTS & TEMPLATES ***/
			
			/**
			 * Since WSAG4J uses WS-Security to digitally sign messages exchanged between client and server, 
			 * a client must provide a set of security credentials.  
			 */
			System.out.println(" 1) Before getLoginContext");
            LoginContext loginContext = getLoginContext();
            
            /**
             * This LoginContext is passed to the AgreementFactoryLocator that allows retrieval of available agreement factory clients 
             * from the deployed wsag4j server identified by its url as shown below. 
             * The client factory array (factories) stores objects of AgreementFactoryClient's type.
             */
            System.out.println(" 2) ");
            EndpointReferenceType wsag4j_server_epr = EndpointReferenceType.Factory.newInstance();
            System.out.println(wsag4j_server_epr);
            wsag4j_server_epr.addNewAddress().setStringValue("http://127.0.0.1:8080/wsag4j-rest-webapp-2.0.0/");
            System.out.println(wsag4j_server_epr);
            System.out.println(" 3) Before retreiving the registry");
            AgreementFactoryRegistryClient registry = AgreementFactoryRegistryLocator.getFactoryRegistry(wsag4j_server_epr, loginContext);
            //System.out.println(" 4) " + registry);
            AgreementFactoryClient[] factories = registry.listAgreementFactories();
            
            System.out.println(" factories length: "+factories.length);
            /**
             * It is possible to get a particular agreement factory specified by an ID. 
             * The agreement factory IDs can be specified in the wasg4j-engine.config file. 
             * In current snippet an agreement factory with ID "SAMPLE-INSTANCE-1" is retrieved:
             */
            System.out.println(" 5)");
            AgreementFactoryClient factory = null;
            
/*            for (int i = 0; i < factories.length; i++) {
            	System.out.println(factories[i].getResourceId());
                if ("CARMEN-INSTANCE-1".equals(factories[i].getResourceId())) { //"SAMPLE-INSTANCE-1"  The same in the line 360
                    factory = factories[i];
                    System.out.println("factory.getResourceId(): "+factory.getResourceId());
                    //break;
                }
            }*/
            
            //The sample wsag4j-engine.config only contains one ResourceId
            factory = factories[0];
            System.out.println("factory.getResourceId(): "+ factory.getResourceId());
  
            
            System.out.println(" 5a) SAMPLE-INSTANCE-1 - SAMPLE-INSTANCE-2 and ISQoS_TEST are in " +
            		"../WEB-INF/classes/wsag4j-engine/instance1 and ../WEB-INF/classes/wsag4j-engine/instance1");
            System.out.println();
            System.out.println(" WSAG4J supports multiple agreement factories within one web application.\n" +
            		" This can be useful e.g. when you have a set of systems that should be accesses via the WS-Agreement protocol.\n" +
            		" Instead of having a separate web application for each system, wsag4j allows you to have one agreement factory\n" +
            		" per system, each factory represented by a separate engine.\n" +
            		" The WSAG4JEngineInstances configuration section allows you to specify a set of configuration files,\n" +
            		" where each configuration file configures a separate AgreementFactory.");
            System.out.println();
            System.out.println(" You have to add the path where the instance is in ../WEB-INF/classes/wsrf-engine.config");
            
            /**
             * After the agreement factory client is created, 
             * this client can be used to retrieve valid agreement templates from the factory. 
             * The next example shows how an agreement factory client retrieves all templates from the agreement factory:
             */
            /**
             * AgreementTemplateType[] templates = factory.getTemplates();
             */
            
            /**
             * Retrieving templates by ID is also possible,for example, 
             * the sample template with template name "SAMPLE-TEMPLATE" and id "1" from the "SAMPLE-INSTANCE-1" agreement factory:
             */
            
             //AgreementTemplateType template2 = factory.getTemplate("SAMPLE-TEMPLATE", "1");
             
             //System.out.println(" ++++ " + template2.xmlText());

            /**
             * After an agreement template was retrieved from the factory, 
             * a client can use this template to create a valid agreement offer. 
             * The agreement offer is a request to create a new service level agreement under the conditions stated in the offer. 
             * An agreement offer can be created by the following code:
             */
            //AgreementOffer offer = new AgreementOfferType(template); <--- This seems to be placed later in the code
            
            //System.out.println(offer.getXMLObject());
            
            /*** NEGOTIATION AGREEMENT ***/
            //NegotiationClient negotiation = factory.initiateNegotiation(arg0); //org.ogf.schemas.graap.wsAgreement.negotiation.NegotiationContextType
            NegotiationClient negotiation = null;
            //
            // initiate the negotiation instance
            //
            negotiation = initiateNegotiation(negotiation, factory);
            
            //
            // retrieve the agreement templates for which negotiation is supported,
            // and select the one with template name "SAMPLE-TEMPLATE".
            //
            System.out.println(" 12) Getting negotiation template ");
            
            AgreementTemplateType[] negotiableTemplates = negotiation.getNegotiableTemplates();
            System.out.println(" 12a) Numbers of negotiable templates : " + negotiableTemplates.length);
            System.out.println("      The List of the negotiable templates \n" +
            				   "      (for one of the instances in ../classes/wsag4j-engine/(instance1, instance2, ISQoSInstance) ) \n" +
            				   "      are in ../classes/samples/ ");
            
            AgreementTemplateType template = null;
            for ( int i = 0; i < negotiableTemplates.length; i++ )
            {
            	AgreementTemplateType agreementTemplate = negotiableTemplates[i];
            	System.out.println();
            	System.out.println( "retrieved template: " + agreementTemplate.getName() + ":" + agreementTemplate.getTemplateId() );
            	System.out.println( agreementTemplate.toString() );
            
            	if ( agreementTemplate.getName().equals( "SAMPLE-TEMPLATE" ) )   // "SAMPLE-TEMPLATE"
            	{
            		template = agreementTemplate;
            	}
            }
            
            System.out.println("\n The tempate is ");
            System.out.println(template.toString());
            /*
            System.out.println( "Overview of the available factories and their templates." );
            for ( int i = 0; i < 2; i++ )
            {
                System.out.println( "+ " + factories[i].getResourceId() );

                AgreementTemplateType[] templates = factories[i].getTemplates();
                System.out.println( "   + Num of templates: " + templates.length );

                for ( int k = 0; k < templates.length; k++ )
                {
                    System.out.println( "      + " + templates[k].getName() );
                
                    if(templates[k].getName().equals("SAMPLE3"))
                    {
                    	System.out.println(" 13) " + templates[k].xmlText());
                    }
                }
            }
			*/
            //
            // The "SAMPLE-TEMPLATE" template exposes the current availability of computing resources by a
            // resource provider.
            //
            System.out.println( );
            System.out.println("The SAMPLE-TEMPLATE template exposes the current availability of computing resources by a resource provider.");
            System.out.println();
			//System.out.println(" 13) " + template.xmlText() );   It's equal to "negotiationTemplate.getXMLObject().xmlText()"
       
			SampleAgreementTemplate negotiationTemplate = new SampleAgreementTemplate(template);
			
//			if ( LOG.isTraceEnabled() )
//			{
//				 System.out.println(" 12b) negotiation-template: " + negotiationTemplate.getXMLObject().getName() ); 
//			     LOG.trace( "negotiation-template: " + negotiationTemplate.getXMLObject().xmlText() );
//			     System.out.println("\n negotiation-template: " + negotiationTemplate.getXMLObject().xmlText() );
//			     LOG.trace(" *** ");
//			     System.out.println(" *** ");
//			     LOG.trace( "negotiation-template: " + negotiationTemplate.getXMLObject().getName() );
//			}else{
//				System.out.println(" *** 13)a NO LOG *** ");
//			}

			/******************************************************************
			* First round of the Negotiation
			******************************************************************/
			//
			// This template will include the service description term 'RESOURCE_SDT' with a JSDL document
			// describing the available compute resources.
			//
			// NegotiationOffer_1: 05 resources for 15 minutes duration with a time frame as
			// startTime = current, endTime = startTime + 60
			//
			NegotiationOfferType counterOffer1 = negotiateRound1( negotiation, negotiationTemplate );

			/******************************************************************
			* Second round of the Negotiation
			******************************************************************/
			//
			// we receive 2 counter offers with different resource availability as
			//
			// CounterOffer_1: 05 resources for 20 minutes duration with a time fame as
			// startTime = current + 5, endTime = startTime + 20
			//
			// CounterOffer_2: 05 resources for 15 minutes duration with a time fame as
			// startTime = current + 10, endTime = startTime + 30
			//
			// Say that both counter offers are not sufficient with respect to time,
			// we create another negotiation offer with
			//
			// NegotiationOffer_2: 05 resources for 15 minutes duration with a time frame as
			// startTime = current + 10, endTime = startTime + 20
			//
			NegotiationOfferType selectedCounterOffer = negotiateRound2( negotiation, counterOffer1 );

			//
			// create agreement if negotiated offer satisfy the requirements
			//
			AgreementOffer offer = new AgreementOfferType( selectedCounterOffer );
			System.out.println(" EPR in client \n" + offer.getInitiatorEPR());
			
			AgreementClient agreement = getFactoryClient().createAgreement( offer );
			Assert.assertNotNull( agreement );
			LOG.info( "negotiated agreement successfully created" );
			
			//
			// After agreement creation, current states of service description term(s) can be queried as shown below:
			//
			
			ServiceTermStateType[] states = agreement.getServiceTermStates();
			System.out.println(" - STATES LENGTH -------------------------- ");
            System.out.println( states.length );
            
			ServiceTermStateType resourceServiceTerm  = agreement.getServiceTermState("RESOURCE_STD");
			System.out.println(" - RESOURCE SERVICE TERM Description ------------------ RESOURCE_STD");
            System.out.println( resourceServiceTerm.xmlText() );
            
			XmlObject[] jobDefinition = resourceServiceTerm.selectChildren(JobDefinitionDocument.type.getDocumentElementName());
			
			System.out.println(" - JOB DEFINITION LENGTH -------------------------- ");
            System.out.println( jobDefinition.length );
            while(jobDefinition.length == 0){
            	System.out.print(".");
            	states = agreement.getServiceTermStates();
            	resourceServiceTerm  = agreement.getServiceTermState("RESOURCE_STD");
            	jobDefinition = resourceServiceTerm.selectChildren(JobDefinitionDocument.type.getDocumentElementName());
            }
            System.out.println(" - JOB DEFINITION LENGTH -------------------------- ");
            System.out.println( jobDefinition.length );
            System.out.println(" - RESOURCE SERVICE TERM Description ----------------- RESOURCE_STD");
            System.out.println( resourceServiceTerm.xmlText() );
            
			JobDefinitionType jobDef = (JobDefinitionType) jobDefinition[0];
			ResourcesType resources = jobDef.getJobDescription().getResources();
			System.out.println( " - RESOURCES ---------------------- " );
			System.out.println( resources.xmlText() );
			
			//TermTreeType TermsAgreementInstance  = agreement.getTerms();
			//System.out.println( " - TERMS Agreement Instance ---------------------- " );
			//System.out.println( TermsAgreementInstance.toString() );
			
			System.out.println( " - HOST NAME ---------------------- " );
			String reservedHostName = resources.getCandidateHosts().getHostNameArray(0); 
			System.out.println( reservedHostName );
			//
			//To access the Guarantee Term States, the getGuaranteeTermStates() method can be used.
			//
			GuaranteeTermStateType[] guaranteeTermStates = agreement.getGuaranteeTermStates();
			System.out.println( " - GUARANTEE TERMS STATES ---------------------- " );
			System.out.println( guaranteeTermStates.length);
			//
			// To get the status of an agreement:
			//
			AgreementStateType state = agreement.getState();

			System.out.println( " ----------------------- " );
			System.out.println( state.toString() );
			
			//
			// finally terminate the negotiation process
			//
			LOG.info( "terminating negotiated agreement" );
			negotiation.terminate();
            
		} 
		catch ( NegotiationException e )
		{
			Assert.fail( "NegotiationException: " + e.getMessage() );
		}
		catch ( ResourceUnavailableException e )
		{
			Assert.fail( "ResourceUnavailableException: " + e.getMessage() );
		}
		catch ( ResourceUnknownException e )
		{
			Assert.fail( "ResourceUnknownException: " + e.getMessage() );
		}
		catch (Exception ex) {
            // catching error
            ex.printStackTrace();
        }

	}
	
	private static AgreementFactoryClient getFactoryClient() throws ResourceUnknownException, ResourceUnavailableException
	{
		AgreementFactoryClient factory=null;
		//
		// First all agreement factories are loaded and then an agreement factory having
		// "SAMPLE-INSTANCE-1" resource id is selected.
		//

		AgreementFactoryClient[] factories = getAgreementFactoryClients();
		System.out.println(" factory.length: " + factories.length);
		//Assert.assertEquals( 3, factories.length ); /* It is 3 because we have SAMPLE-INSTANCE-1, SAMPLE-INSTANCE-1, ISQoS_TEST
		LOG.info( "factories: " + factories.length );

		/*
		if ( factories[0].getResourceId().equals( "CARMEN-INSTANCE-1" ) )   // "SAMPLE-INSTANCE-1"  The same in line 114
		{
			System.out.println(" *** This is   CARMEN-INSTANCE-1 ");
			factory = getAgreementFactoryClients()[0];
		}
		else
		{
			System.out.println(" *** This is NOT  CARMEN-INSTANCE-1 ");
			factory = getAgreementFactoryClients()[1];
		}
		*/
		for( int i=0; i < factories.length; i++)
		{
			if( factories[i].getResourceId().equals( "CARMEN-INSTANCE-1" ) )
			{
				factory = getAgreementFactoryClients()[i];
			}
			System.out.println(" *** factories[i].getResourceId() " + factories[i].getResourceId());
		}
		return factory;
	}
	
	/**
	 * 
	* @return the agreement factory clients 
	*/
	public static AgreementFactoryClient[] getAgreementFactoryClients()
	{
		AgreementFactoryClient[] factories = null;
		try
		{
			LoginContext loginContext = getLoginContext();
	 
			// START SNIPPET: ListAgreementFactories
			EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
			epr.addNewAddress().setStringValue( "http://127.0.0.1:8080/wsag4j-agreement-factory-1.0.2" );
	 
			AgreementFactoryRegistryClient registry = AgreementFactoryRegistryLocator.getFactoryRegistry( epr, loginContext );
			factories = registry.listAgreementFactories();
			// END SNIPPET: ListAgreementFactories
		}
		catch ( Exception ex )
		{
			LOG.error( ex );
		}
	return factories;
	}

	
	private static NegotiationOfferType negotiateRound2( NegotiationClient negotiation, NegotiationOfferType counterOffer1 ) throws Exception
	{
	/******************************************************************
	* Second round of the Negotiation
	******************************************************************/
	//
	// we receive 2 counter offers with different resource availability as
	//
	// CounterOffer_1: 05 resources for 20 minutes duration with a time fame as
	// startTime = current + 5, endTime = startTime + 20
	//
	// CounterOffer_2: 05 resources for 15 minutes duration with a time fame as
	// startTime = current + 10, endTime = startTime + 30
	//
	// Say that both counter offers are not sufficient with respect to time,
	// we create another negotiation offer with
	//
	// NegotiationOffer_2: 05 resources for 15 minutes duration with a time frame as
	// startTime = current + 10, endTime = startTime + 20
	//
		SampleNegotiationOffer negotiationOffer2 = new SampleNegotiationOffer( counterOffer1 );

		ResourcesType jobResources2 = negotiationOffer2.getResourceDefinition();
		
		RangeValueType totalCountRange2 = RangeValueType.Factory.newInstance();
		totalCountRange2.addNewExact().setDoubleValue( 5 );
		jobResources2.setTotalResourceCount( totalCountRange2 );
		
		TimeConstraintType timeConstraint2 = negotiationOffer2.getTimeConstraint();
		
		Calendar startTime2 = (Calendar) timeConstraint2.getStartTime().clone();
		startTime2.add( Calendar.MINUTE, 10 );
		Calendar endTime2 = (Calendar) startTime2.clone();
		endTime2.add( Calendar.MINUTE, 20 );
		timeConstraint2.setStartTime( startTime2 );
		timeConstraint2.setEndTime( endTime2 );
		timeConstraint2.setDuration( 15 );

		setResourcesSDT( negotiationOffer2, jobResources2 );
		setTimeConstraintSDT( negotiationOffer2, timeConstraint2 );

		NegotiationOfferType[] negotiationOfferTypes2 = { negotiationOffer2.getXMLObject() };
		if ( LOG.isTraceEnabled() )
		{
			for ( int i = 0; i < negotiationOfferTypes2.length; i++ )
			{
				LOG.trace( "Iteration-2: negotiation offers: " + negotiationOfferTypes2[i].toString() );
			}
		}

		NegotiationOfferType[] counterOffers2 = negotiation.negotiate( negotiationOfferTypes2 );
		Assert.assertNotNull( counterOffers2 );
		Assert.assertEquals( 1, counterOffers2.length );
		
		System.out.println();
		LOG.info( "Iteration-2: Number of counter offers received: " + counterOffers2.length );

		if ( LOG.isTraceEnabled() )
		{
			for ( int i = 0; i < counterOffers2.length; i++ )
			{
				System.out.println();
				LOG.info( "Iteration: Number of counter offer received " + i );
				System.out.println();
				LOG.trace( "Iteration-2: counter_offer: " + counterOffers2[i].xmlText() );
			}
		}
		
		LOG.info( "second iteration of negotiation is successful" );

		NegotiationOfferType selectedCounterOffer = counterOffers2[0];

		//
		// check whether the negotiation (counter) offer is rejected
		//
		if ( selectedCounterOffer.getNegotiationOfferContext().getState().isSetRejected() )
		{
			String message = "Iteration-2: counter offer [" + selectedCounterOffer.getOfferId()
							+ "] is rejected. Reason: "
							+ selectedCounterOffer.getNegotiationOfferContext().getState().xmlText();
			LOG.error( message );
			Assert.fail( "Iteration-2: NegotiationException: " + message );
		}


		return selectedCounterOffer;
	}
	
	private static NegotiationOfferType negotiateRound1( NegotiationClient negotiation, SampleAgreementTemplate negotiationTemplate ) throws Exception
	{
		/******************************************************************
		* First round of the Negotiation
		******************************************************************/
		//
		// This template includes the service description term 'RESOURCE_SDT' with a JSDL document
		// describing the available compute resources.
		//
		// NegotiationOffer_1: 05 resources for 15 minutes duration with a time frame as
		// startTime = current, endTime = startTime + 60
		//
		//
		String offerID = negotiationTemplate.getContext().getTemplateId() + "-" + negotiationTemplate.getName();
		System.out.println();
		System.out.println(" 14) " + offerID);
		//String offerID2 = negotiationTemplate.getTemplateId() + "-" + negotiationTemplate.getName();
		//System.out.println(" 14a) " + offerID2); // This seems to be equal to 14)
		ResourcesType jobResources1 = negotiationTemplate.getResourceDefinition();
		System.out.println();
		System.out.println(" 14a) \n" + jobResources1);
		
		RangeValueType totalCountRange1 = RangeValueType.Factory.newInstance();
		totalCountRange1.addNewExact().setDoubleValue( 5 );
		jobResources1.setTotalResourceCount( totalCountRange1 );
		System.out.println();
		System.out.println(" 14b) \n" + jobResources1);
		
		//
		// The service description term 'TIME_CONSTRAINT_SDT' defines the time frame
		// during which the resources can be available.
		// The start and end time specified by a user is considered to be the earliest 
		// possible start time and the deadline.
		// We need an advance reservation of the resources for 15 minutes effective duration, 
		// however, within a same time frame as received from a resource provider
		//
		TimeConstraintType timeConstraint1 = negotiationTemplate.getTimeConstraint();
		System.out.println();
		System.out.println(" 14c) \n" + timeConstraint1);
		
		Calendar startTime1 = (Calendar) timeConstraint1.getStartTime().clone();
		Calendar endTime1 = (Calendar) timeConstraint1.getEndTime().clone();
		
		timeConstraint1.setStartTime( startTime1 );
		timeConstraint1.setEndTime( endTime1 );
		timeConstraint1.setDuration( 15 );

		System.out.println();
		System.out.println(" 14d) \n" + timeConstraint1);
		//
		// now we create negotiation offer from a negotiable template for the reservation of
		// required resources to fulfil the application execution requirements.
		//
		SampleNegotiationOffer negotiationOffer1 = negotiationTemplate.getNegotiationOffer(); // Taking Context and Terms from "negotiationTemplate"
		negotiationOffer1.setOfferId( offerID );
		System.out.println();
		System.out.println(" 14f) \n" + negotiationOffer1.getXMLObject().xmlText()); 
		System.out.println(" 14f) \n" + negotiationOffer1.getXMLObject().toString()); 
		//
		// creating negotiation offer context
		//
		NegotiationOfferContextType negOfferContext = NegotiationOfferContextType.Factory.newInstance();
		negOfferContext.setCreator( NegotiationRoleType.NEGOTIATION_INITIATOR );
		GregorianCalendar expireDate = new GregorianCalendar();
		expireDate.add( Calendar.MINUTE, 5 );
		negOfferContext.setExpirationTime( expireDate );
		System.out.println();
		System.out.println(" 14g) \n" + negOfferContext);
		
		NegotiationOfferStateType negOfferState = NegotiationOfferStateType.Factory.newInstance();
		negOfferState.addNewAdvisory();
		negOfferContext.setState( negOfferState );
		System.out.println();
		System.out.println(" 14h) \n" + negOfferContext);
		
		negOfferContext.setCounterOfferTo( offerID ); // a fully qualified name of the template
		                                              // (templateID-TemplateName)
		System.out.println();
		System.out.println(" 14i) \n" + negOfferContext);

		negotiationOffer1.setNegotiationOfferContext( negOfferContext );
		System.out.println();
		//System.out.println(" 14j) \n" + negotiationOffer1.getXMLObject().xmlText());
		System.out.println(" 14j) \n" + negotiationOffer1.getXMLObject().toString());
		
		//
		// one negotiation constraint is added that basically define the preferred time frame window within
		// which the user would like to have the reservation of resources (say within first 45 minutes)
		//
		NegotiationConstraintSectionType constraints1 = addNeogtiationOfferConstraints( timeConstraint1.getStartTime() );
		negotiationOffer1.setNegotiationConstraints( constraints1 );
		System.out.println();
		System.out.println(" 14m) \n" + negotiationOffer1.getXMLObject().toString());
		//
		// SDT Service Description Terms 'RESOURCE_SDT' and 'TIME_CONSTRAINT_SDT' are updated in a negotiation offer.
		//
		setResourcesSDT( negotiationOffer1, jobResources1 );
		setTimeConstraintSDT( negotiationOffer1, timeConstraint1 );
		
		NegotiationOfferType[] negotiationOfferTypes1 = { negotiationOffer1.getXMLObject() };
		
		System.out.println();
		if ( LOG.isTraceEnabled() )
		{
			for ( int i = 0; i < negotiationOfferTypes1.length; i++ )
			{
				LOG.trace( "Iteration-1: negotiation offers: " + negotiationOfferTypes1[i].toString() );
			}
		}
		
		//
        // invoking negotiate method from Negotiation instance and
		// in return counter offers are received
		//
		NegotiationOfferType[] counterOffers1 = negotiation.negotiate( negotiationOfferTypes1 );
		//assertNotNull( counterOffers1 );
		//assertEquals( 2, counterOffers1.length );

		System.out.println();
		LOG.info( "Iteration-1: Number of counter offers received: " + counterOffers1.length );

		if ( LOG.isTraceEnabled() )
		{
			for ( int i = 0; i < counterOffers1.length; i++ )
			{
				System.out.println();
				System.out.println(" >>> Counter Offer Number: " + i);
				System.out.println();
				LOG.trace( "Iteration-1: counter_offer: " + counterOffers1[i].xmlText() );
			}
		}
		
		//
		// first check whether the negotiation (counter) offer is rejected
		//
		NegotiationOfferType counterOffer1 = counterOffers1[0];
		if ( counterOffer1.getNegotiationOfferContext().getState().isSetRejected() )
		{
			String message = "Iteration-1: counter offer [" + counterOffer1.getOfferId() + "] is rejected. Reason: " 
							+ counterOffer1.getNegotiationOfferContext().getState().xmlText();
			LOG.error( message );
			System.out.println();
			System.out.println(" Error: " + message);
			//fail( "Iteration-1: NegotiationException: " + message );
		}
		
		System.out.println();
		System.out.println(" counterOffer1: " + counterOffer1.xmlText());
		System.out.println();
		LOG.info( "first iteration of negotiation is successful" );

		return counterOffer1; 
	}
	
	private static void setTimeConstraintSDT( SampleNegotiationOffer negotiationOffer, TimeConstraintType timeConstraint )
	{
	 
		ServiceDescriptionTermType timeConstraintSDT = null;
	 
	    ServiceDescriptionTermType[] sdts =
	    negotiationOffer.getTerms().getAll().getServiceDescriptionTermArray();
	 
	    if ( sdts != null )
	    {
	    	for ( int i = 0; i < sdts.length; i++ )
	        {
	    		if ( sdts[i].getName().equals( "TIME_CONSTRAINT_SDT" ) )
	            {
	    			timeConstraintSDT = sdts[i];
	                break;
	            }
	        }
	    }
	 
	    String name = timeConstraintSDT.getName();
	    String serviceName = timeConstraintSDT.getServiceName();
	      
	    TimeConstraintDocument timeConstraintDoc = TimeConstraintDocument.Factory.newInstance();
	    timeConstraintDoc.addNewTimeConstraint();
	    timeConstraintDoc.getTimeConstraint().set( timeConstraint );
	    
	    timeConstraintSDT.set( timeConstraintDoc );
	    timeConstraintSDT.setName( name );
	    timeConstraintSDT.setServiceName( serviceName );
	    
	    System.out.println();
		System.out.println(" 14r) timeConstraintSDT: \n" + timeConstraintSDT.xmlText());
	}

	
	private static void setResourcesSDT( SampleNegotiationOffer negotiationOffer, ResourcesType jobResources ) throws Exception
	{
		ServiceDescriptionTermType resourcesSDT = null;
		ServiceDescriptionTermType[] sdts = negotiationOffer.getTerms().getAll().getServiceDescriptionTermArray();
		if ( sdts != null )
		{
			for ( int i = 0; i < sdts.length; i++ )
			{
				System.out.println();
				System.out.println(" 14n) \n" + sdts[i].getName());
				if ( sdts[i].getName().equals( "RESOURCE_STD" ) )
				{
					resourcesSDT = sdts[i];
					break;
				}
			}
		}
		
		System.out.println();
		System.out.println(" 14n)a) resourcesSDT: \n" + resourcesSDT.xmlText());
		
		String name = resourcesSDT.getName();
		String serviceName = resourcesSDT.getServiceName();
		System.out.println();
		System.out.println(" 14o) name: " + name + " - serviceName: " + serviceName);
		
		JobDefinitionDocument resourcesDoc = JobDefinitionDocument.Factory.newInstance();
		resourcesDoc.addNewJobDefinition().addNewJobDescription().addNewResources();
		resourcesDoc.getJobDefinition().getJobDescription().getResources().set( jobResources );

		System.out.println();
		System.out.println(" 14p) \n" + resourcesDoc.toString());
		
		resourcesSDT.set( resourcesDoc );
		resourcesSDT.setName( name );
		resourcesSDT.setServiceName( serviceName );
		
		System.out.println();
		System.out.println(" 14q) resourcesSDT: \n" + resourcesSDT.xmlText());
		
	}
	
	private static NegotiationConstraintSectionType addNeogtiationOfferConstraints( Calendar startTime )
	{
	
		final String constraintItemName = "TimeConstraintSDT_TimeConstraint_START_TIME";
		final String constraintXpath = 
				"declare namespace wsag-tc='http://schemas.wsag4j.org/2009/07/wsag4j-scheduling-extensions';"
	          + "declare namespace wsag='http://schemas.ggf.org/graap/2007/03/ws-agreement';"
	          + "$this/wsag:Terms/wsag:All/wsag:ServiceDescriptionTerm[@wsag:Name = 'TIME_CONSTRAINT_SDT']"
	          + "/wsag4jt:TimeConstraint";
	
	    NegotiationConstraintSectionType constraints = NegotiationConstraintSectionType.Factory.newInstance();
	    
	    // add one item constraint
	    NegotiationOfferItemType offerItem = constraints.addNewItem();
	    offerItem.setName( constraintItemName );
	    offerItem.setLocation( constraintXpath );
	    System.out.println();
		System.out.println(" 14k) \n" + offerItem.toString());
		
	    ItemConstraint constraint = offerItem.addNewItemConstraint();
	    constraint.addNewMinInclusive().setValue( XmlDateTime.Factory.newValue( startTime ) );
	    Calendar preferredEndTime = (Calendar) startTime.clone();
	    preferredEndTime.add( Calendar.MINUTE, 20 );
	    constraint.addNewMaxInclusive().setValue( XmlDateTime.Factory.newValue( preferredEndTime ) );
	    System.out.println();
		System.out.println(" 14l) \n" + constraint.xmlText());
		
	    return constraints;
	}


	private static NegotiationClient initiateNegotiation(NegotiationClient negotiation, AgreementFactoryClient factory)
	{
		try
		{
			// Now creates a negotiation context that defines the roles and obligations
			// of the negotiating parties and specifies the type of the negotiation process.
			//
			NegotiationContextDocument negContextDoc = NegotiationContextDocument.Factory.newInstance();
			System.out.println(" 6) " + negContextDoc.xmlText());
			NegotiationContextType negContext = negContextDoc.addNewNegotiationContext();
			//negContext.setAgreementFactoryEPR( factory.getEndpoint() );
			negContext.setAgreementFactoryEPR( factory.getRemoteClient().getRemoteReference() );
			System.out.println(" 7) " + negContext.xmlText() );
			negContext.setAgreementResponder( NegotiationRoleType.NEGOTIATION_RESPONDER);
			System.out.println(" 8) " + negContext.xmlText() );
			
			GregorianCalendar expireDate = new GregorianCalendar();
			expireDate.add( Calendar.HOUR, 1 );
			negContext.setExpirationTime( expireDate );
			System.out.println( );
			System.out.println(" 9) " + negContext.xmlText() );
			
			//
			// set the nature of the negotiation process (e.g. negotiation or re-negotiation).
			//
			NegotiationType negotiationType = negContext.addNewNegotiationType();
			negotiationType.addNewNegotiation();  // <--- It seems I'm not using negotiationType
			// negotiationType.addNewRenegotiation();   <---- RE-negotiation
			System.out.println( );
			System.out.println(" 10) " + negContext.xmlText() );
			
			//
			// creating negotiation instance based on a negotiation context from a selected agreement factory
			//
			negotiation = factory.initiateNegotiation( negContext );
			System.out.println( );
			System.out.println(" 11) " + negotiation.getNegotiationContext().xmlText() );
			System.out.println( );
			System.out.println(" 11a) *** Negotiation instance is created successfully ***" );
/*		}
		catch ( NegotiationFactoryException e )
		{
			System.out.println( "NegotiationFactoryException: " + e.getMessage() );
		}
		catch ( ResourceUnavailableException e )
		{
			System.out.println( "ResourceUnavailableException: " + e.getMessage() );
		}
		catch ( ResourceUnknownException e )
		{
			System.out.println( "ResourceUnknownException: " + e.getMessage() );
*/		}
		catch ( Exception e )
		{
			System.out.println( "Could not create negotiation client instance. Error: " + e.getMessage() );
		}

		return negotiation;
	}


	
	/**
	 * A client must provide a set of security credentials. 
	 * The easiest way to provide these credentials is to provide a valid LoginContext as shown below:
	 * 
	 * @return LoginContext
	 * @throws LoginException
	 */
	private static LoginContext getLoginContext() throws LoginException {
        /*
        * create a keystore login context
        */
       System.out.println("Beginning");
       KeystoreProperties properties = new KeystoreProperties();
       properties.setKeyStoreAlias("wsag4j-user");
       properties.setPrivateKeyPassword("user@wsag4j");

       System.out.println(" 1.a)");
       properties.setKeyStoreType("JKS");
       properties.setKeystoreFilename("/wsag4j-client-keystore.jks");
       properties.setKeystorePassword("user@wsag4j");

       System.out.println(" 1.b)");
       properties.setTruststoreType("JKS");
       properties.setTruststoreFilename("/wsag4j-client-keystore.jks");
       properties.setTruststorePassword("user@wsag4j");

       System.out.println(" 1.c)");
       LoginContext loginContext = new KeystoreLoginContext(properties);
       System.out.println(" 1.d)");
       loginContext.login();
       System.out.println(" 1.e)");

       return loginContext;
   }

}
