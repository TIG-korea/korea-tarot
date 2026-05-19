export function validateConcern(value: string) {
  const concern = value.trim();
  if (concern.length < 10) {
    return "고민은 최소 10자 이상 입력해야 합니다.";
  }
  if (concern.length > 1000) {
    return "고민은 1000자를 초과할 수 없습니다.";
  }
  if (/^(.)\1{9,}$/.test(concern)) {
    return "반복 문자만으로는 상담을 시작할 수 없습니다.";
  }
  return null;
}

export function validateEmail(value: string) {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim())) {
    return "올바른 이메일 형식이 아닙니다.";
  }
  return null;
}

export function validatePassword(value: string) {
  if (value.length < 8) {
    return "비밀번호는 최소 8자 이상이어야 합니다.";
  }
  return null;
}

export function validateNickname(value: string) {
  const nickname = value.trim();
  if (nickname.length < 2 || nickname.length > 20) {
    return "닉네임은 2자 이상 20자 이하로 입력해야 합니다.";
  }
  return null;
}
