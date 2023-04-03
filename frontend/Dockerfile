FROM node:19.7 as node

ENV GIT_REPO_URL=https://github.com/netology-code/jd-homeworks.git
ENV GIT_REPO_SUBDIR=diploma/netology-diplom-frontend

WORKDIR /src
RUN git clone --filter=blob:none --sparse $GIT_REPO_URL git-repo
WORKDIR /src/git-repo
RUN git sparse-checkout add $GIT_REPO_SUBDIR

WORKDIR /src/git-repo/$GIT_REPO_SUBDIR
RUN npm install

ENV VUE_APP_BASE_URL=/api
RUN npm run build

FROM nginx
EXPOSE 80

ENV GIT_REPO_SUBDIR=diploma/netology-diplom-frontend
COPY --from=node /src/git-repo/$GIT_REPO_SUBDIR/dist /usr/share/nginx/html/
COPY nginx-default.conf /etc/nginx/conf.d/default.conf
