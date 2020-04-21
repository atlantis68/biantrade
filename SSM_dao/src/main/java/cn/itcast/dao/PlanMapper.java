package cn.itcast.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.itcast.pojo.Plan;

public interface PlanMapper {

	public int insertPlan(Plan plan);

	public int updatePlanById(Plan plan);

	public List<Plan> findPlanByUid(@Param("uid") String uid);
	
	public List<Plan> findCachePlanByUid(@Param("uid") String uid);
	
	public List<Plan> findFllowPlans(@Param("level") Integer level);
	
	public Plan findPlanById(@Param("id") Integer id);
	
	public List<Plan> findPlansById(@Param("id") Integer id);
	
	public List<Plan> findPlansByState(@Param("state") String state);
	
	public List<Plan> findPlansByState1();
	
	public List<Plan> findPlanByTime(@Param("time") Integer time, @Param("level") Integer level);
	
	public int updateStrategyById(Plan plan);
}
