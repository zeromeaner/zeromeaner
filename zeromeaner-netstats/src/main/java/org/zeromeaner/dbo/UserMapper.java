package org.zeromeaner.dbo;

public interface UserMapper {
	public User selectId(Integer userId);
	public User selectEmail(String email);
	public void insert(User user);
	public void update(User user);
}
