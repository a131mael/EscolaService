
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

import org.aaf.dto.AlunoAulaDTO;
import org.aaf.dto.AulaDTO;
import org.escola.model.AlunoAula;
import org.escola.model.Aula;
import org.escola.model.Member;
import org.escola.util.Service;


@Stateless
public class AulaService extends Service implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "EscolaDS")
	private EntityManager em;

	public Aula findById(EntityManager em, Long id) {
		return em.find(Aula.class, id);
	}

	public Aula findById(Long id) {
		return em.find(Aula.class, id);
	}
	
	public Member findMemberById(Long id) {
		return em.find(Member.class, id);
	}
	
	
	public Aula findByCodigo(Long id) {
		return em.find(Aula.class, id);
	}
	
	public String remover(Long idAula){
		em.remove(findById(idAula));
		return "index";
	}

	public List<Aula> findAll() {
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aula> criteria = cb.createQuery(Aula.class);
			Root<Aula> member = criteria.from(Aula.class);
			criteria.select(member).orderBy(cb.desc(member.get("dataParaExibicao")));
			return em.createQuery(criteria).getResultList();
	
		}catch(NoResultException nre){
			return new ArrayList<>();
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public List<Aula> findAll(String idMember) {
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aula> criteria = cb.createQuery(Aula.class);
			Root<Aula> member = criteria.from(Aula.class);
			
			
		//	Predicate where = null;

			//where = cb.equal(member.get("codigo"), codigo); fazer sql para somente o Aula da pessoa
			
			criteria.select(member).orderBy(cb.desc(member.get("dataParaExibicao")));
			return em.createQuery(criteria).getResultList();
	
		}catch(NoResultException nre){
			return new ArrayList<>();
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public List<AlunoAula> findbyMemberDate(String idMember,Date data) {
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<AlunoAula> criteria = cb.createQuery(AlunoAula.class);
			Root<AlunoAula> member = criteria.from(AlunoAula.class);
			
			
			Predicate where1 = cb.equal(member.get("aluno").get("id"), idMember);
			Predicate where2 = cb.equal(member.get("aula").get("dataAula"), data); 

			criteria.select(member).where(where1, where2);
			return em.createQuery(criteria).getResultList();
	
		}catch(NoResultException nre){
			return new ArrayList<>();
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<AlunoAulaDTO> findbyMemberDateDTO(String idMember,Date data) {
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<AlunoAula> criteria = cb.createQuery(AlunoAula.class);
			Root<AlunoAula> member = criteria.from(AlunoAula.class);
			CriteriaQuery cq = criteria.select(member);
			
			Predicate where1 = cb.equal(member.get("aluno").get("id"), idMember);
			Predicate where2 = cb.equal(member.get("aula").get("dataAula"), data); 

			cq.where(where1, where2);
			cq.orderBy(cb.asc(member.get("aula").get("ordem")));
			
			 List<AlunoAula> alunosAula = em.createQuery(criteria).getResultList();
			 List<AlunoAulaDTO> alunosAulaDTO = new ArrayList<>();
			
			 for(AlunoAula aa : alunosAula){
				 alunosAulaDTO.add(aa.getDTO());
			 }
			return alunosAulaDTO;
	
		}catch(NoResultException nre){
			return new ArrayList<>();
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<AulaDTO> findAllDTO(){
		List<AulaDTO> aulasDto = new ArrayList<>();
		for(Aula r : findAll()){
			aulasDto.add(r.getDTO());
		}
		
		return aulasDto;
	}
	
	public List<AulaDTO> findAllDTO(String idMember){
		List<AulaDTO> aulasDto = new ArrayList<>();
		for(Aula r : findAll(idMember)){
			aulasDto.add(r.getDTO());
		}
		
		return aulasDto;
	}

	public Aula save(Aula aula) {
		Aula user = null;
		try {

		
			if (aula.getId() != null && aula.getId() != 0L) {
				user = findById(aula.getId());
			} else {
				user = new Aula();
			}
			
			user.setDataAula(aula.getDataAula());
			user.setDescricao(aula.getDescricao());
			user.setDisciplina(aula.getDisciplina());
			user.setId(aula.getId());
			user.setLinkYoutube(aula.getLinkYoutube());
			user.setOrdem(aula.getOrdem());
			user.setSerie(aula.getSerie());
			user.setTitulo(aula.getTitulo());
			user.setVisible(aula.getVisible());
			
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

	public Aula findByCodigo(String codigo) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aula> criteria = cb.createQuery(Aula.class);
			Root<Aula> member = criteria.from(Aula.class);

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
	public List<Aula> find(int first, int size, String orderBy, String order, Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aula> criteria = cb.createQuery(Aula.class);
			Root<Aula> member = criteria.from(Aula.class);
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
			return (List<Aula>) q.getResultList();

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
			Root<Aula> member = countQuery.from(Aula.class);
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

	
}

