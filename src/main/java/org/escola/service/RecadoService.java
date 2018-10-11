
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
import org.escola.model.Member;
import org.escola.model.Recado;
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
			criteria.select(member).orderBy(cb.desc(member.get("dataParaExibicao")));
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

	

}

