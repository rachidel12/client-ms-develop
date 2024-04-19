package ai.geteam.client.service.recruiter;


import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.feign.IamService;
import ai.geteam.client.mapper.RecruiterMapper;
import org.springframework.stereotype.Component;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.entity.recruiter.Status;
import ai.geteam.client.entity.signatue.Signature;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.utils.ErrorCode;
import ai.geteam.client.helper.JwtHelper;
import ai.geteam.client.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;


@Component
@Slf4j
@RequiredArgsConstructor
public class RecruiterValidator {

	private final JwtHelper jwtHelper;
	private final RecruiterRepository rep;
	private final IamService iamService;

	// Definition of constants for error messages
	public static final String USER_EMAIL_NOT_FOUND = "User email not found";
	public static final String INVALIDTOKEN = "Invalid Token";
	

	//verify that the recuiter is ACTIVE before he Can do any Action
	public void validateStatusActive(String authorization) {
		try {
		// extrat the sub-recruiter(who's have the rights to activate a status )
		String myEmail= jwtHelper.extractEmail(authorization);
		Optional<Recruiter> recruiter = rep.findByEmail(myEmail);
		// verify if that recruiter is present in the database
		if (!recruiter.isPresent()){
			log.error(USER_EMAIL_NOT_FOUND);
            throw new InvalidInputException(ErrorCode.NO_USER_WITH_THIS_EMAIL,USER_EMAIL_NOT_FOUND);
	       }
		// verify if he's have the rights to do actions 
		if(recruiter.get().getStatus() != Status.ACTIVE) {
				log.error("User IS BLOCKED CAN'T DO ANY ACTION");
				throw new InvalidInputException(ErrorCode.USER_IS_BLOCKED,"User is blocked ");
			}

		}catch (InvalidInputException e) {
			throw e;
		}catch (Exception e) {
	        log.error(INVALIDTOKEN, e.getMessage());
	        throw new InvalidInputException(ErrorCode.ACCESS_TOKEN_INVALID,INVALIDTOKEN);
	    }
	}

	//verify that the recruiter is an admin
	public void validateAdmin(String authorization) {
		try {
		// extrat the sub-recruiter(who's have the rights to activate a status )
		String myEmail= jwtHelper.extractEmail(authorization);
		Optional<Recruiter> recruiter = rep.findByEmail(myEmail);
		// verify if that recruiter is present in the database
		if (!recruiter.isPresent()){
			log.error(USER_EMAIL_NOT_FOUND);
            throw new InvalidInputException(ErrorCode.NO_USER_WITH_THIS_EMAIL,USER_EMAIL_NOT_FOUND);
        }
		// verify if he's an admin or not 
		if(!recruiter.get().isAdmin()) {
			log.error("User not admin");
			throw new InvalidInputException(ErrorCode.USER_NOT_ADMIN,"The user is not an admin");
		}
		}catch (InvalidInputException e) {
			throw e;
		}catch (Exception e) {
	        log.error(INVALIDTOKEN, e.getMessage());
	        throw new InvalidInputException(ErrorCode.ACCESS_TOKEN_INVALID,INVALIDTOKEN);
	    }
    }
	
	//verify that the Admin recruiter and the blocked recruiter are in the same company
	public void validateSameCompany(Recruiter blockedRecruiter,String authorization) {
		try {
		String myEmail= jwtHelper.extractEmail(authorization);
		Optional<Recruiter> recruiter = rep.findByEmail(myEmail);
		if (!recruiter.isPresent()){
			log.error(USER_EMAIL_NOT_FOUND);
            throw new InvalidInputException(ErrorCode.NO_USER_WITH_THIS_EMAIL,USER_EMAIL_NOT_FOUND);
        }
		Long myCompanyId = recruiter.get().getCompany().getId();
		Long recruiterCompanyId = blockedRecruiter.getCompany().getId();
		if (!Objects.equals(myCompanyId, recruiterCompanyId)) {
			log.error("User not in the  same team");
			throw new InvalidInputException(ErrorCode.USER_NOT_IN_SAME_TEAM,"user not in the same team");
		}
		}catch (InvalidInputException e) {
			throw e;
		}catch (Exception e) {
	        log.error(INVALIDTOKEN, e.getMessage());
	        throw new InvalidInputException(ErrorCode.ACCESS_TOKEN_INVALID,INVALIDTOKEN);
	    }
    }
	
	//VERIFY THE STATUS
	public void validateRecruiterStatus(Recruiter recruiter) {
		log.error("User Already activate");
        if (recruiter.getStatus() != Status.BLOCKED) {
            throw new InvalidInputException(ErrorCode.USER_ALREADY_ACTIVATED,"User Already activate");
        }
    }

	public Long extractCompanyId(String token) {
		return Optional.ofNullable(jwtHelper.extractEmail(token))
				.flatMap(rep::findByEmail)
				.map(Recruiter::getCompany)
				.map(Company::getId)
				.orElse(null);
	}
     public boolean verifyIsAdmin (String authorization){
            boolean isAdmin = Optional.ofNullable(jwtHelper.extractEmail(authorization))
                    .flatMap(rep::findByEmail)
                    .map(Recruiter::isAdmin)
                    .orElse(false);
            if (!isAdmin) {
                log.error("You are not an admin ");
                throw new InvalidInputException(ErrorCode.USER_NOT_ADMIN, "You are not an admin");
            }
            return true;
        }


	// Vérifiez que l'utilisateur et la signature appartient au  meme entreprise
		public void validateSignatureCompany(String authorization, Signature signature){
			try {
				String myEmail= jwtHelper.extractEmail(authorization);
				Optional<Recruiter> recruiter = rep.findByEmail(myEmail);
				if (!recruiter.isPresent()){
					log.error(USER_EMAIL_NOT_FOUND);
		            throw new InvalidInputException(ErrorCode.NO_USER_WITH_THIS_EMAIL,USER_EMAIL_NOT_FOUND);
		        }
				Long myCompanyId = recruiter.get().getCompany().getId();
				Long signatureID = signature.getCompany().getId();
				if (!Objects.equals(myCompanyId, signatureID)) {
					log.error("User not in the  same team");
					throw new InvalidInputException(ErrorCode.USER_NOT_IN_SAME_TEAM,"user not in the same team");
				}
		}catch (InvalidInputException e) {
			throw e;
		}catch (Exception e) {
	        log.error(INVALIDTOKEN, e.getMessage());
	        throw new InvalidInputException(ErrorCode.ACCESS_TOKEN_INVALID,INVALIDTOKEN);
	    }
		}

        public boolean userDoesNotExist (String email,String authorization){
            Optional<Recruiter> recruiterOptional = rep.findByEmail(email);
            boolean userExistsInKeycloak = !iamService.getUserByUsername("client", email,authorization).isEmpty();
            if (recruiterOptional.isPresent() || userExistsInKeycloak) {
                log.error("Recruiter already exist");
                throw new InvalidInputException(ErrorCode.EMAIL_ALREADY_EXISTS, "Recruiter already exist");
            }
            return true;
        }

        public Long addNewUserToDatabase (RecruiterDTO recruiterDTO){
            Recruiter newRecruiter = RecruiterMapper.toRecruiter(recruiterDTO);
            Long companyId = recruiterDTO.getCompanyId();
            Company company = new Company();
            company.setId(companyId);
            newRecruiter.setCompany(company);
            Recruiter savedRecruiter = rep.save(newRecruiter);
            return savedRecruiter.getId();
        }
    }
