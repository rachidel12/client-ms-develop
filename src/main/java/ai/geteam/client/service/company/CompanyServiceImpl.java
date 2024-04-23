package ai.geteam.client.service.company;


import ai.geteam.client.dto.CompanyCountryInfoDTO;
import ai.geteam.client.dto.CompanyDTO;
import ai.geteam.client.dto.HiringConsultantDTO;
import ai.geteam.client.dto.RecruiterDTO;
import ai.geteam.client.entity.Company;
import ai.geteam.client.entity.location.City;
import ai.geteam.client.entity.location.Country;
import ai.geteam.client.entity.location.State;
import ai.geteam.client.entity.recruiter.Recruiter;
import ai.geteam.client.exception.DuplicationException;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.exception.utils.ErrorCode;
import ai.geteam.client.helper.JwtHelper;
import ai.geteam.client.mapper.CompanyCountryInfoMapper;
import ai.geteam.client.mapper.CompanyMapper;
import ai.geteam.client.mapper.RecruiterMapper;
import ai.geteam.client.repository.*;
import ai.geteam.client.service.recruiter.RecruiterValidator;
import ai.geteam.client.utils.CompanyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


@Service
@Log4j2
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    private final RecruiterRepository recruiterRepository;

    private final CityRepository cityRepository;

    private final JwtHelper jwtHelper;

    private final StateRepository stateRepository;

    private final CountryRepository countryRepository;

    private static final String CLIENT_INVALID_ERROR_MSG = "Client Invalid";

    private static final String CLIENT_INVALID_ERROR_MSG2 = "City not found";

    private final RecruiterValidator validator;

    @Override
    public String create(CompanyDTO companyDTO) {
        log.info("Receved companyDTO : "+companyDTO);

        if (!CompanyValidator.isValid(companyDTO)) {
            log.error(CLIENT_INVALID_ERROR_MSG);
            throw new InvalidInputException(ErrorCode.CLIENT_INVALID, CLIENT_INVALID_ERROR_MSG);
        }

        RecruiterDTO recruiterDTO = companyDTO.getRecruiter();
        if (emailInUse(recruiterDTO.getEmail())) {
            log.error("Email already in use");
            throw new DuplicationException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already in use");
        }

        Company company = CompanyMapper.toCompany(companyDTO);

        City city = cityRepository.findById(companyDTO.getCityId()).orElseThrow(() -> {
            log.error(CLIENT_INVALID_ERROR_MSG2);
            return new InvalidInputException(ErrorCode.CLIENT_INVALID, CLIENT_INVALID_ERROR_MSG2);
        });


        Recruiter recruiter = RecruiterMapper.toRecruiter(recruiterDTO);


        company.setCity(city);
        company.setState(city.getState());
        company.setCountry(city.getCountry());
        company.addRecruiter(recruiter);
        company = companyRepository.save(company);
        log.info("Company well saved");
        return company.getId().toString();
    }


    @Override
    public String delete(Long id) {
        companyRepository.deleteById(id);
        return "Deleted";
    }

    private boolean emailInUse(String email) {
        return recruiterRepository.existsByEmail(email);
    }

    private static boolean isValidWebsite(String website) {
        return website != null
                && Pattern.matches("^https://[a-zA-Z0-9.\\-/]+$", website);
    }

    private static boolean isValidSize(String size) {
        return size != null
                && Pattern.matches("^\\d+-\\d+$", size)
                && Integer.parseInt(size.split("-")[0]) < Integer.parseInt(size.split("-")[1]);
    }

    private static boolean isValidName(String name) {
        return name != null
                && Pattern.matches("^[a-zA-Z0-9]{1,30}$", name);
    }

    @Override
    public CompanyDTO updateCompanyById(CompanyDTO companyDTO, String token) {
        if (!isValidWebsite(companyDTO.getWebsite())) {
            throw new InvalidInputException(ErrorCode.GENERAL_EXCEPTION, "Invalid Website");
        }
        if (!isValidSize(companyDTO.getSize())) {
            throw new InvalidInputException(ErrorCode.GENERAL_EXCEPTION, "Invalid Size");
        }
        if (!isValidName(companyDTO.getName())) {
            throw new InvalidInputException(ErrorCode.GENERAL_EXCEPTION, "Invalid Name");
        }
        Company company = companyRepository.findById(companyDTO.getId())
                .orElseThrow(() ->
                        new InvalidInputException(ErrorCode.GENERAL_EXCEPTION, "Company with id: " + companyDTO.getId() + " not found"));


        //email from token
        String email = jwtHelper.extractEmail(token);

        //recruiters on company
        List<Recruiter> recruiters = company.getRecruiters();


// recruiter2 is the recruiter doing the request based on the  email retrieved from token
        Recruiter recruiter = recruiters.stream().filter(r ->
                        r.getEmail().equals(email)
                ).findFirst()
                .orElseThrow(() ->
                        new UnAuthorizedException(ErrorCode.USER_NOT_ADMIN, "Not a Recruiter for this Company"));


        if (!recruiter.isAdmin()) {
            throw new UnAuthorizedException(ErrorCode.USER_NOT_ADMIN, "Not an Admin for this Company");
        }


        City city = cityRepository.findById(companyDTO.getCityId()).orElseThrow(() -> {
            log.error(CLIENT_INVALID_ERROR_MSG2);
            return new InvalidInputException(ErrorCode.CLIENT_INVALID, "Country not found");
        });


        Country country = countryRepository.findById(companyDTO.getCountryId()).orElseThrow(() -> {
            log.error(CLIENT_INVALID_ERROR_MSG2);
            return new InvalidInputException(ErrorCode.CLIENT_INVALID, "Country not found");
        });


        State state = stateRepository.findById(companyDTO.getStateId()).orElseThrow(() -> {
            log.error("State not found");
            return new InvalidInputException(ErrorCode.CLIENT_INVALID, "State not found");
        });


        company.setName(companyDTO.getName());
        company.setWebsite(companyDTO.getWebsite());
        company.setSize(companyDTO.getSize());
        company.setCountry(country);
        company.setCity(city);
        company.setState(state);


        return CompanyMapper.toCompanyDTO(companyRepository.save(company));

    }


    @Override
    public CompanyCountryInfoDTO getCompanyInfo(String token) {
        Long companyId = validator.extractCompanyId(token);
        if (companyId != null) {
            Optional<Company> companyOptional = companyRepository.findById(companyId);
            if (companyOptional.isPresent()) {
                return CompanyCountryInfoMapper.toDto(companyOptional.get());
            }
        }
        return null;
    }

    @Override
    public HiringConsultantDTO getHiringConsultant(String token) {
        Long companyId = validator.extractCompanyId(token);
        if (companyId == null) {
            log.error(CLIENT_INVALID_ERROR_MSG);
            throw new InvalidInputException(ErrorCode.CLIENT_INVALID, CLIENT_INVALID_ERROR_MSG);
        }
        Company company = companyRepository.findById(companyId).orElseThrow(() -> {
            log.error(CLIENT_INVALID_ERROR_MSG);
            return new InvalidInputException(ErrorCode.CLIENT_INVALID, CLIENT_INVALID_ERROR_MSG);
        });

        return CompanyMapper.toHiringConsultantDto(company);


    }
}
