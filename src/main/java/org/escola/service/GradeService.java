
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
import org.escola.enums.BimestreEnum;
import org.escola.enums.DisciplinaEnum;
import org.escola.model.Aluno;
import org.escola.model.AlunoAula;
import org.escola.model.AlunoAvaliacao;
import org.escola.model.Recado;
import org.escola.util.Service;


@Stateless
public class GradeService extends Service implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "EscolaDS")
	private EntityManager em;
	
	
	public Map<BimestreEnum,Map<DisciplinaEnum,String>> notas;
	
	public  Map<BimestreEnum,Map<DisciplinaEnum,String>> getGrades(Long idAluno){
		Map<BimestreEnum,Map<DisciplinaEnum,String>> grades = new HashMap<>();
		
		
		
		//grades.put(BimestreEnum.PRIMEIRO_BIMESTRE, value)
		
		return grades;
	}
	
	
	public AlunoAula setWhatched(Long id) {
		AlunoAula user = null;
		try {

			user = em.find(AlunoAula.class, id);
			
			user.setAssistiu(true);
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

	
	
	public float getAVGrade(Long idAluno, DisciplinaEnum disciplina, BimestreEnum bimestre, boolean recupecacao,int anoLetivo) {
		try {
			if (idAluno == 5506L) {
				System.out.println("id");
			}
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT av from  AlunoAvaliacao av ");
			sql.append("where 1 = 1");
			sql.append(" and  av.aluno.id = ");
			sql.append(idAluno);
			sql.append(" and  av.avaliacao.disciplina = ");
			sql.append(disciplina.ordinal());
			if (bimestre != null) {
				sql.append(" and  av.avaliacao.bimestre = ");
				sql.append(bimestre.ordinal());
			}
			sql.append(" and  av.avaliacao.recuperacao = ");
			sql.append(recupecacao);

			sql.append(" and  av.avaliacao.anoLetivo = ");
			sql.append(anoLetivo);
			Query query = em.createQuery(sql.toString());

			List<AlunoAvaliacao> notas = (List<AlunoAvaliacao>) query.getResultList();
			Float soma = 0F;
			Float pesos = 0F;
			if (notas != null && !notas.isEmpty()) {
				for (AlunoAvaliacao avas : notas) {
					soma += avas.getNota() * avas.getAvaliacao().getPeso();
					pesos += avas.getAvaliacao().getPeso();
				}
			}

			return soma / pesos;

		} catch (Exception e) {
			return 0f;
		}
	}

}

