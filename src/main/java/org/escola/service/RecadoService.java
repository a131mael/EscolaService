
package org.escola.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.aaf.dto.RecadoDTO;
import org.aaf.dto.RecadoDestinatarioDTO;
import org.escola.enums.TipoDestinatario;
import org.escola.model.Member;
import org.escola.model.Recado;
import org.escola.model.RecadoCurtido;
import org.escola.model.RecadoDescurtido;
import org.escola.model.RecadoDestinatario;
import org.escola.util.Service;


@Stateless
public class RecadoService extends Service implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "EscolaDS")
	private EntityManager em;

	public Recado findById(EntityManager em, Long id) {
		return em.find(Recado.class, id);
	}

	public Recado findById(Long id) {
		return em.find(Recado.class, id);
	}
	
	public Member findMemberById(Long id) {
		return em.find(Member.class, id);
	}
	
	public RecadoCurtido findCurtiuByIdRecado(Long id, Long idUsuario) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<RecadoCurtido> criteria = cb.createQuery(RecadoCurtido.class);
			Root<RecadoCurtido> member = criteria.from(RecadoCurtido.class);
			
			Predicate whereRecado = null;
			Predicate whereUsuario = null;
			Recado rec = findById(id);
			Member mem = findMemberById(idUsuario);

			whereRecado = cb.equal(member.get("recado"), rec);
			whereUsuario = cb.equal(member.get("curtiu"), mem);
			
			criteria.select(member).where(whereRecado,whereUsuario);

			criteria.select(member);
			return em.createQuery(criteria).getSingleResult();

		} catch (NoResultException nre) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	public RecadoDescurtido findDescurtiuByIdRecado(Long id, Long idUsuario) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<RecadoDescurtido> criteria = cb.createQuery(RecadoDescurtido.class);
			Root<RecadoDescurtido> member = criteria.from(RecadoDescurtido.class);

			Predicate whereRecado = null;
			Predicate whereUsuario = null;
			Recado rec = findById(id);
			Member mem = findMemberById(idUsuario);

			whereRecado = cb.equal(member.get("recado"), rec);
			whereUsuario = cb.equal(member.get("curtiu"), mem);
			
			criteria.select(member).where(whereRecado,whereUsuario);

			criteria.select(member);
			return em.createQuery(criteria).getSingleResult();

		} catch (NoResultException nre) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	private RecadoDestinatario findByIdRecadoDestinatario(Long id) {
		return em.find(RecadoDestinatario.class, id);
	}

	
	public Recado findByCodigo(Long id) {
		return em.find(Recado.class, id);
	}
	
	public String remover(Long idRecado){
		em.remove(findById(idRecado));
		return "index";
	}

	public List<Recado> findAll() {
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Recado> criteria = cb.createQuery(Recado.class);
			Root<Recado> member = criteria.from(Recado.class);
			CriteriaQuery cq = criteria.select(member);
			
			Predicate where1 = cb.equal(member.get("aprovado"), true);
			cq.where(where1);
			
			cq.orderBy(cb.desc(member.get("dataParaExibicao")));
			return em.createQuery(criteria).getResultList();
	
		}catch(NoResultException nre){
			return new ArrayList<>();
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public List<Recado> findAll(String idMember) {
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Recado> criteria = cb.createQuery(Recado.class);
			Root<Recado> member = criteria.from(Recado.class);
			
			
		//	Predicate where = null;

			//where = cb.equal(member.get("codigo"), codigo); fazer sql para somente o recado da pessoa
			
			criteria.select(member).orderBy(cb.desc(member.get("dataParaExibicao")));
			return em.createQuery(criteria).getResultList();
	
		}catch(NoResultException nre){
			return new ArrayList<>();
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public List<RecadoDTO> findAllDTO(){
		List<RecadoDTO> recadosDto = new ArrayList<>();
		for(Recado r : findAll()){
			recadosDto.add(r.getDTO());
		}
		
		return recadosDto;
	}
	
	public List<RecadoDTO> findAllDTO(String idMember){
		List<RecadoDTO> recadosDto = new ArrayList<>();
		for(Recado r : findAll(idMember)){
			recadosDto.add(r.getDTO());
		}
		
		return recadosDto;
	}

	public Recado save(Recado recado) {
		Recado user = null;
		try {

			log.info("Registering " + recado.getNome());
		
			if (recado.getId() != null && recado.getId() != 0L) {
				user = findById(recado.getId());
			} else {
				user = new Recado();
			}
			
			user.setFilePergunta(recado.getFilePergunta());
			user.setDataFim(recado.getDataFim());
			user.setDataInicio(recado.getDataInicio());
			user.setDescricao(recado.getDescricao());
			user.setNome(recado.getNome());
			user.setCodigo(recado.getCodigo());
			user.setDataParaExibicao(recado.getDataInicio()!=null?recado.getDataInicio():new Date());
			
			em.persist(user);
			
		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			// builder = createViolationResponse(ce.getConstraintViolations());
		} catch (ValidationException e) {
			// Handle the unique constrain violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("email", "Email taken");

		} catch (Exception e) {
			// Handle generic exceptions
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("error", e.getMessage());

			e.printStackTrace();
		}

		return user;
	}

	public Recado findByCodigo(String codigo) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Recado> criteria = cb.createQuery(Recado.class);
			Root<Recado> member = criteria.from(Recado.class);

			Predicate whereSerie = null;

			whereSerie = cb.equal(member.get("codigo"), codigo);
			criteria.select(member).where(whereSerie);

			criteria.select(member);
			return em.createQuery(criteria).getSingleResult();

		} catch (NoResultException nre) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Recado> find(int first, int size, String orderBy, String order, Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Recado> criteria = cb.createQuery(Recado.class);
			Root<Recado> member = criteria.from(Recado.class);
			CriteriaQuery cq = criteria.select(member);

			final List<Predicate> predicates = new ArrayList<Predicate>();
			for (Map.Entry<String, Object> entry : filtros.entrySet()) {

				Predicate pred = cb.and();
				if (entry.getValue() instanceof String) {
					pred = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" + entry.getValue() + "%"));
				} else {
					pred = cb.equal(member.get(entry.getKey()), entry.getValue());
				}
				 predicates.add(pred);
				//cq.where(pred);
			}

			cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
			cq.orderBy((order.equals("asc") ? cb.asc(member.get(orderBy)) : cb.desc(member.get(orderBy))));
			Query q = em.createQuery(criteria);
			q.setFirstResult(first);
			q.setMaxResults(size);
			return (List<Recado>) q.getResultList();

		} catch (NoResultException nre) {
			return new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}

	}

	public long count(Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Recado> member = countQuery.from(Recado.class);
			countQuery.select(cb.count(member));
			
			final List<Predicate> predicates = new ArrayList<Predicate>();
			if (filtros != null) {
				for (Map.Entry<String, Object> entry : filtros.entrySet()) {

					Predicate pred = cb.and();
					if (entry.getValue() instanceof String) {
						pred = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" + entry.getValue() + "%"));
					} else {
						pred = cb.equal(member.get(entry.getKey()), entry.getValue());
					}
					predicates.add(pred);
				}
			}
			countQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
			Query q = em.createQuery(countQuery);
			return (long) q.getSingleResult();

		} catch (NoResultException nre) {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void saveAwnser(RecadoDestinatarioDTO dto) {
		RecadoDestinatario user = null;
		try {

			RecadoDestinatarioDTO recDes = findRecadoDestinatario(dto.getRecado().getId()+"", dto.getDestinatario().getId()+"");

			if(recDes == null){
				log.info("Registering recadoDestinatario" + dto.getId());
				
				if (dto.getId() != null && dto.getId() != 0L) {
					user = findByIdRecadoDestinatario(dto.getId());
				} else {
					user = new RecadoDestinatario();
				}
				user.setDestinatario(em.find(Member.class, dto.getDestinatario().getId()));
				user.setRecado(em.find(Recado.class, dto.getRecado().getId()));
				user.setResposta(dto.getResposta());
				user.setRespostaExtenso(dto.getRespostaExtenso());
				
				
				em.persist(user);
			}
			
		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			// builder = createViolationResponse(ce.getConstraintViolations());
		} catch (ValidationException e) {
			// Handle the unique constrain violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("email", "Email taken");

		} catch (Exception e) {
			// Handle generic exceptions
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("error", e.getMessage());

			e.printStackTrace();
		}

//		return user;
		
		
	}
	
	
	public Recado save2(RecadoDTO recado) {
		Recado user = null;
		try {

			log.info("Registering " + recado.getNome());

			if (recado.getId() != null && recado.getId() != 0L) {
				user = findById(recado.getId());
			} else {
				user = new Recado();
			}

			user.setDataFim(recado.getDataFim());
			user.setDataInicio(recado.getDataInicio());
			user.setDescricao(recado.getDescricao());
			user.setNome(recado.getNome());
			user.setCodigo(recado.getCodigo());
			user.setBigQuestion(recado.isBigQuestion());
			user.setDescricaoUnder(recado.getDescricaoUnder());
			user.setFilePergunta(recado.getFilePergunta());
			user.setFontSizeQuestion(recado.getFontSizeQuestion());
			user.setId(recado.getId());
			user.setOpcao1(recado.getOpcao1());
			user.setOpcao2(recado.getOpcao2());
			user.setOpcao3(recado.getOpcao3());
			user.setOpcao4(recado.getOpcao4());
			user.setOpcao5(recado.getOpcao5());
			user.setOpcao6(recado.getOpcao6());
			user.setRespostaBooleana(recado.isRespostaBooleana());
			user.setQuestionario(recado.isQuestionario());
			user.setRespostaAberta(recado.getRespostaAberta());
			user.setDataParaExibicao(recado.getDataInicio() != null ? recado.getDataInicio() : new Date());
			user.setTipoDestinatario(TipoDestinatario.values()[recado.getTipoDestinatario()]);

			em.persist(user);

		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			// builder = createViolationResponse(ce.getConstraintViolations());
		} catch (ValidationException e) {
			// Handle the unique constrain violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("email", "Email taken");

		} catch (Exception e) {
			// Handle generic exceptions
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("error", e.getMessage());

			e.printStackTrace();
		}

		return user;
	}

	
	public RecadoDestinatarioDTO findRecadoDestinatario(String idRecado, String idDestinatario) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<RecadoDestinatario> criteria = cb.createQuery(RecadoDestinatario.class);
			Root<RecadoDestinatario> member = criteria.from(RecadoDestinatario.class);

			Predicate whereLogin = null;
			Predicate whereSenha = null;

			whereLogin = cb.equal(member.get("recado").get("id"), idRecado);
			whereSenha = cb.equal(member.get("destinatario").get("id"), idDestinatario);
			criteria.select(member).where(whereLogin,whereSenha);

			criteria.select(member);
			return em.createQuery(criteria).getSingleResult().getDTO();

		} catch (NoResultException nre) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public void  curtir(String idRecado, String usuario){
		
		Recado recado = findById(Long.parseLong(idRecado));
		RecadoCurtido rc = findCurtiuByIdRecado(Long.parseLong(idRecado),Long.parseLong(usuario));
		if(rc == null){
			rc = new RecadoCurtido();
			rc.setCurtiu(findMemberById(Long.parseLong(usuario)));
			rc.setRecado(recado);
			em.persist(rc);
			
			RecadoDescurtido rcdesc = findDescurtiuByIdRecado(Long.parseLong(idRecado),Long.parseLong(usuario));
			if(rcdesc != null){
				em.remove(rcdesc);
			}
		}
	}
	
	public void desCurtir(String idRecado, String usuario){

		Recado recado = findById(Long.parseLong(idRecado));

		RecadoDescurtido rc = findDescurtiuByIdRecado(Long.parseLong(idRecado),Long.parseLong(usuario));
		if(rc == null){
			rc = new RecadoDescurtido();
			rc.setCurtiu(findMemberById(Long.parseLong(usuario)));
			rc.setRecado(recado);
			em.persist(rc);
			
			
			RecadoCurtido rcur = findCurtiuByIdRecado(Long.parseLong(idRecado),Long.parseLong(usuario));
			if(rcur != null){
				em.remove(rcur);
			}
		}
	}
	
	public boolean curti(String idRecado, String idUsuario){
		RecadoCurtido rcur = findCurtiuByIdRecado(Long.parseLong(idRecado),Long.parseLong(idUsuario));
		if(rcur == null){
			return false;
		}else{
			return true;
		}
	}
	
	public boolean descurti(String idRecado, String idUsuario){
		RecadoDescurtido rc = findDescurtiuByIdRecado(Long.parseLong(idRecado),Long.parseLong(idUsuario));
		if(rc == null){
			return false;
		}else{
			return true;
		}
	}
}

